package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionLogger;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * The IExecutionContext interface defines the execution context for a blueprint execution process.
 * It encapsulates the runtime environment, tracking mechanisms, and safety limits required for
 * safe and efficient blueprint execution.
 *
 * Responsibilities are organized into five main areas:
 * 1. Variable Management - Runtime variable storage and retrieval via IVariableRegistry
 * 2. Data Flow Resolution - Pin value resolution for data flow between nodes
 * 3. Environmental Context - Access to the Minecraft world and entity context
 * 4. Execution Tracking - Monitoring execution progress and enforcing safety limits
 * 5. Debug Logging - Recording execution steps for debugging and profiling
 */
public interface IExecutionContext {

    /**
     * Returns the variable registry for this execution context.
     * The registry provides type-safe access to runtime variables.
     *
     * @return the variable registry instance
     */
    IVariableRegistry getVariables();

    /**
     * Gets the value of a pin by resolving its connected output pin.
     * If the pin is an input pin with a connected output pin:
     * - Returns the cached value if already computed
     * - Otherwise, evaluates the source node (if pure) and returns the value
     *
     * For pure nodes (nodes without execution pins), this triggers on-demand evaluation.
     *
     * @param pin the input pin to get the value for
     * @return the value of the connected output pin, or null if not connected or no value
     */
    @Nullable
    Object get_pin_value(IBlueprintPin pin);

    /**
     * Sets the value of an output pin.
     * This is called by nodes during execution to publish their output values.
     *
     * @param pin the output pin to set the value for
     * @param value the value to set
     */
    void set_pin_value(IBlueprintPin pin, @Nullable Object value);

    /**
     * Returns the Minecraft world in which this blueprint is executing.
     *
     * @return the world instance, or null if no world context is available
     */
    @Nullable
    World getWorld();

    /**
     * Returns the entity associated with this execution context.
     * This is typically set when executing entity blueprints.
     *
     * @return the entity instance, or null if no entity context is available
     */
    @Nullable
    Entity getEntity();

    /**
     * Returns the node currently being executed.
     * This is primarily used for debugging and error reporting.
     *
     * @return the current node, or null if no node is currently executing
     */
    @Nullable
    IBlueprintNode get_current_node();

    /**
     * Sets the current node being executed.
     * This method is called internally by the blueprint executor.
     *
     * @param node the node that is about to execute
     */
    void set_current_node(IBlueprintNode node);

    /**
     * Returns the instant when execution started.
     * Used to calculate elapsed time and enforce timeout limits.
     *
     * @return the execution start instant
     */
    Instant get_start_time();

    /**
     * Returns the number of nodes executed so far in this execution.
     * Used to enforce the maximum node execution limit.
     *
     * @return the number of nodes executed
     */
    int get_nodes_executed();

    /**
     * Increments the counter tracking how many nodes have been executed.
     * This method is called internally by the blueprint executor.
     */
    void increment_nodes();

    /**
     * Returns the maximum execution time allowed for this blueprint.
     * Default is 50ms to prevent server lag.
     *
     * @return the timeout duration
     */
    Duration getTimeout();

    /**
     * Returns the maximum number of nodes that can be executed.
     * Default is 10,000 to prevent infinite loops.
     *
     * @return the maximum node count
     */
    int get_max_nodes();

    /**
     * Checks whether execution should stop due to timeout or node limit.
     * This method is called before executing each node.
     *
     * @return true if execution should stop, false otherwise
     */
    boolean isStoppingNeeded();

    /**
     * Returns the execution logger for recording execution steps.
     * Used for debugging, profiling, and analyzing blueprint execution.
     *
     * @return the execution logger instance
     */
    IExecutionLogger getLogger();

    /**
     * Gets the current iteration count for a specific loop node.
     * Used to track how many times a loop node has executed.
     *
     * @param node the loop node to check
     * @return the current iteration count, or 0 if the node hasn't iterated yet
     */
    int getIterations(IBlueprintNode node);

    /**
     * Increments the iteration counter for a specific loop node.
     * This is called each time a loop node executes.
     *
     * @param node the loop node to increment
     */
    void increment_iterations(IBlueprintNode node);

    /**
     * Resets the iteration counter for a specific loop node.
     * This is called when a loop completes or needs to restart.
     *
     * @param node the loop node to reset
     */
    void reset_iterations(IBlueprintNode node);

    /**
     * Checks if a loop node has exceeded the maximum iteration limit.
     * This prevents infinite loops from hanging the server.
     *
     * @param node the loop node to check
     * @return true if the iteration limit has been exceeded
     */
    boolean hasExceededIterations(IBlueprintNode node);

    /**
     * Returns the blueprint scheduler for tick-based async operations.
     * Used by nodes like DelayNode to schedule delayed execution.
     *
     * @return the scheduler instance
     */
    IBlueprintScheduler getScheduler();
}
