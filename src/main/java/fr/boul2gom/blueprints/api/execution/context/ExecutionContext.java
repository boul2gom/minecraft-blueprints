package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionLogger;
import fr.boul2gom.blueprints.api.execution.monitoring.IExecutionMonitor;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.execution.context.IterationTracker;
import fr.boul2gom.blueprints.execution.context.VariableProvider;
import fr.boul2gom.blueprints.execution.debug.ExecutionLogger;
import fr.boul2gom.blueprints.execution.monitoring.ExecutionMonitor;
import fr.boul2gom.blueprints.execution.planning.PinResolver;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Refactored implementation of IExecutionContext using provider pattern.
 * Delegates responsibilities to specialized providers:
 * - VariableProvider: Variable storage and retrieval
 * - PinResolver: Pin value resolution and pure node evaluation
 * - IterationTracker: Loop iteration tracking
 * - ExecutionMonitor: Safety limits and execution statistics
 *
 * Thread-safety: SINGLE-THREADED ASYNC
 * - All operations execute on the same thread (Minecraft server thread)
 * - CompletableFuture chains execute sequentially on server thread via executor
 * - NOT SAFE for concurrent access from multiple threads
 * - Each ExecutionContext instance must be used by only one execution at a time
 * - Do NOT share an ExecutionContext across multiple concurrent blueprint executions
 * - Create a new ExecutionContext for each blueprint execution
 * - Internal state (current_node, pin values, iteration counters) is not synchronized
 * - The async execution model (.thenComposeAsync) runs all callbacks on the server thread,
 *   ensuring sequential access to this context even though execution is asynchronous
 *
 * Resource management: AUTOCLOSEABLE
 * - Implements AutoCloseable for proper resource cleanup
 * - close() clears all internal caches and state
 * - Recommended usage: try-with-resources pattern
 */
public class ExecutionContext implements IExecutionContext {

    private final IVariableProvider variable_provider;
    private final IVariableRegistry variable_registry;

    private final PinResolver pin_resolver;
    private final IIterationTracker iteration_tracker;
    private final IExecutionMonitor execution_monitor;
    private final IExecutionLogger logger;

    private final World world;
    private final Entity entity;
    private final Duration timeout;
    private final int max_nodes;

    private IBlueprintNode current_node;

    public ExecutionContext(@Nullable World world, @Nullable Entity entity) {
        this(world, entity, IBlueprintExecutor.MAX_EXECUTION_TIME, IBlueprintExecutor.MAX_NODES_PER_EXECUTION);
    }

    public ExecutionContext(@Nullable World world, @Nullable Entity entity, Duration timeout, int max_nodes) {
        this(world, entity, timeout, max_nodes, true);
    }

    public ExecutionContext(@Nullable World world, @Nullable Entity entity, Duration timeout, int max_nodes, boolean logging) {
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive: " + timeout);
        }
        if (max_nodes <= 0) {
            throw new IllegalArgumentException("Max nodes must be positive: " + max_nodes);
        }

        // Initialize providers
        this.variable_registry = new VariableRegistry();
        this.variable_provider = new VariableProvider(this.variable_registry);

        this.iteration_tracker = new IterationTracker();
        this.execution_monitor = new ExecutionMonitor();
        this.logger = new ExecutionLogger(logging);

        this.world = world;
        this.entity = entity;
        this.timeout = timeout;
        this.max_nodes = max_nodes;
        this.current_node = null;

        // Initialize PinResolver with callbacks (no two-phase initialization)
        this.pin_resolver = new PinResolver(
            () -> this.current_node,
            node -> this.current_node = node,
            node -> node.execute(this)  // Returns CompletableFuture
        );

        // Start monitoring
        this.execution_monitor.start();
    }

    @Override
    public IVariableRegistry getVariables() {
        return this.variable_registry;
    }

    @Override
    @Nullable
    public Object get_pin_value(IBlueprintPin pin) {
        return this.pin_resolver.resolve(pin).orElse(null);
    }

    @Override
    public void set_pin_value(IBlueprintPin pin, @Nullable Object value) {
        this.pin_resolver.set(pin, value);
    }

    @Override
    @Nullable
    public World getWorld() {
        return this.world;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    @Nullable
    public IBlueprintNode get_current_node() {
        return this.current_node;
    }

    @Override
    public void set_current_node(IBlueprintNode node) {
        this.current_node = node;
    }

    @Override
    public Instant get_start_time() {
        return this.execution_monitor.get_start_time();
    }

    @Override
    public int get_nodes_executed() {
        return this.execution_monitor.get_node_count();
    }

    @Override
    public void increment_nodes() {
        this.execution_monitor.increment_node_count();
    }

    @Override
    public Duration getTimeout() {
        return this.timeout;
    }

    @Override
    public int get_max_nodes() {
        return this.max_nodes;
    }

    @Override
    public boolean isStoppingNeeded() {
        return this.execution_monitor.has_timed_out(this.timeout.toMillis())
            || this.execution_monitor.has_exceeded_node_limit(this.max_nodes);
    }

    @Override
    public IExecutionLogger getLogger() {
        return this.logger;
    }

    @Override
    public int getIterations(IBlueprintNode node) {
        return this.iteration_tracker.getCount(node);
    }

    @Override
    public void increment_iterations(IBlueprintNode node) {
        this.iteration_tracker.increment(node);
    }

    @Override
    public void reset_iterations(IBlueprintNode node) {
        this.iteration_tracker.reset(node);
    }

    @Override
    public boolean has_exceeded_iterations(IBlueprintNode node) {
        return this.iteration_tracker.hasExceeded(node, IBlueprintExecutor.MAX_ITERATIONS_PER_LOOP);
    }

    /**
     * Sets a pin value that will be applied to the next frame when push_frame() is called.
     * This ensures loop output pins (like ForLoop's index) are accessible in the loop body frame.
     *
     * Usage pattern:
     * 1. Loop node calculates output values (e.g., current index)
     * 2. Loop node calls set_pin_value_for_next_frame(index_pin, current_index)
     * 3. Loop node returns Set.of("loop_body")
     * 4. Executor calls push_frame()
     * 5. Loop body nodes can now access the index value
     *
     * @param pin The output pin to set
     * @param value The value to store in the next frame
     */
    public void set_pin_value_for_next_frame(IBlueprintPin pin, Object value) {
        this.pin_resolver.set_pin_value_for_next_frame(pin, value);
    }

    /**
     * Pushes a new execution frame for loop isolation.
     * Call this before entering a loop body.
     *
     * @param owner The loop node creating the frame
     */
    public void push_frame(IBlueprintNode owner) {
        this.pin_resolver.push_frame(owner);
    }

    /**
     * Pops the current execution frame.
     * Call this after exiting a loop body.
     */
    public void pop_frame() {
        this.pin_resolver.pop_frame();
    }

    /**
     * Cleans up all resources used by this execution context.
     * Clears caches, resets counters, and releases references.
     *
     * This method is idempotent and can be called multiple times safely.
     * After calling close(), the context should not be reused.
     */
    @Override
    public void close() {
        // Clear pin value cache
        this.pin_resolver.clear_cache();

        // Clear iteration counters
        this.iteration_tracker.clear();

        // Clear variables
        this.variable_provider.clear();

        // Clear execution log
        this.logger.clear();

        // Reset monitoring state (optional, as these are just counters)
        // execution_monitor.reset() - not called as it would affect ongoing monitoring
    }

    @Override
    public String toString() {
        return String.format("ExecutionContext(world=%s, entity=%s, timeout=%dms, max_nodes=%d, nodes_executed=%d)",
            this.world != null ? this.world.getRegistryKey().getValue() : "null",
            this.entity != null ? this.entity.getType().getName().getString() : "null",
            this.timeout.toMillis(),
            this.max_nodes,
            this.get_nodes_executed());
    }
}
