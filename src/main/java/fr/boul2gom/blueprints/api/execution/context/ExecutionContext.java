package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionLogger;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinDirection;
import fr.boul2gom.blueprints.execution.BlueprintScheduler;
import fr.boul2gom.blueprints.execution.debug.ExecutionLogger;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of IExecutionContext.
 * Provides a complete execution environment for blueprint execution including
 * variable storage, data flow resolution, world/entity context, and safety limit enforcement.
 */
public class ExecutionContext implements IExecutionContext {

    private final IVariableRegistry variables;
    private final IExecutionLogger logger;
    private final World world;
    private final Entity entity;
    private final Instant start_time;
    private final Duration timeout;
    private final int max_nodes;

    private final Map<IBlueprintPin, Object> pin_values;
    private final Map<IBlueprintNode, Integer> iterations;

    private IBlueprintNode current_node;
    private int nodes_executed;

    public ExecutionContext(@Nullable World world, @Nullable Entity entity) {
        this(world, entity, IBlueprintExecutor.MAX_EXECUTION_TIME, IBlueprintExecutor.MAX_NODES_PER_EXECUTION);
    }

    public ExecutionContext(@Nullable World world, @Nullable Entity entity, Duration timeout, int max_nodes) {
        this(world, entity, timeout, max_nodes, true);
    }

    public ExecutionContext(@Nullable World world, @Nullable Entity entity, Duration timeout, int max_nodes, boolean logging) {
        this.variables = new VariableRegistry();
        this.logger = new ExecutionLogger(logging);
        this.start_time = Instant.now();
        this.world = world;
        this.entity = entity;
        this.timeout = timeout;
        this.max_nodes = max_nodes;
        this.pin_values = new HashMap<>();
        this.iterations = new HashMap<>();

        this.current_node = null;
        this.nodes_executed = 0;
    }

    @Override
    public IVariableRegistry getVariables() {
        return this.variables;
    }

    @Override
    @Nullable
    public Object get_pin_value(IBlueprintPin pin) {
        if (pin.getDirection() != PinDirection.INPUT) {
            // Only input pins can request values
            return null;
        }

        // If value already cached, return it
        if (this.pin_values.containsKey(pin)) {
            return this.pin_values.get(pin);
        }

        // Find connected output pin
        if (!pin.isConnected()) {
            return null;
        }

        // Get the connected output pin (input pins have at most 1 connection)
        final IBlueprintPin output_pin = pin.getPins().stream()
            .filter(p -> p.getDirection() == PinDirection.OUTPUT)
            .findFirst()
            .orElse(null);

        if (output_pin == null) {
            return null;
        }

        // If the output pin already has a cached value, return it
        if (this.pin_values.containsKey(output_pin)) {
            return this.pin_values.get(output_pin);
        }

        // If source node is pure (no exec pins), evaluate it on-demand
        final IBlueprintNode source_node = output_pin.getNode();
        if (this.is_pure_node(source_node)) {
            // Save current node and restore after
            final IBlueprintNode previous_node = this.current_node;
            this.current_node = source_node;

            // Execute the pure node
            source_node.execute(this);

            // Restore previous node
            this.current_node = previous_node;

            // Return the value that should now be cached
            return this.pin_values.get(output_pin);
        }

        return null;
    }

    @Override
    public void set_pin_value(IBlueprintPin pin, @Nullable Object value) {
        this.pin_values.put(pin, value);
    }

    /**
     * Checks if a node is pure (has no execution pins).
     * Pure nodes are evaluated on-demand when their outputs are needed.
     */
    private boolean is_pure_node(IBlueprintNode node) {
        // Check if any input or output pin is an execution pin
        for (final IBlueprintPin input : node.getInputs()) {
            if (input.isExecution()) {
                return false;
            }
        }
        for (final IBlueprintPin output : node.getOutputs()) {
            if (output.isExecution()) {
                return false;
            }
        }
        return true;
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
        return this.start_time;
    }

    @Override
    public int get_nodes_executed() {
        return this.nodes_executed;
    }

    @Override
    public void increment_nodes() {
        this.nodes_executed++;
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
        // Check if execution time exceeded
        final Duration elapsed_time = Duration.between(this.start_time, Instant.now());
        if (elapsed_time.compareTo(this.timeout) > 0) {
            return true;
        }

        // Check if node limit exceeded
        return this.nodes_executed >= this.max_nodes;
    }

    @Override
    public IExecutionLogger getLogger() {
        return this.logger;
    }

    @Override
    public int getIterations(IBlueprintNode node) {
        return this.iterations.getOrDefault(node, 0);
    }

    @Override
    public void increment_iterations(IBlueprintNode node) {
        final int current = this.getIterations(node);
        this.iterations.put(node, current + 1);
    }

    @Override
    public void reset_iterations(IBlueprintNode node) {
        this.iterations.remove(node);
    }

    @Override
    public boolean hasExceededIterations(IBlueprintNode node) {
        return this.getIterations(node) >= IBlueprintExecutor.MAX_ITERATIONS_PER_LOOP;
    }

    @Override
    public IBlueprintScheduler getScheduler() {
        return BlueprintScheduler.getInstance();
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
