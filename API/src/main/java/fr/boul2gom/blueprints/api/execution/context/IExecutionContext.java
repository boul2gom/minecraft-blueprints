package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
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
 * Responsibilities are organized into three main areas:
 * 1. Variable Management - Runtime variable storage and retrieval via IVariableRegistry
 * 2. Environmental Context - Access to the Minecraft world and entity context
 * 3. Execution Tracking - Monitoring execution progress and enforcing safety limits
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
    IBlueprintNode getCurrentNode();

    /**
     * Sets the current node being executed.
     * This method is called internally by the blueprint executor.
     *
     * @param node the node that is about to execute
     */
    void setCurrentNode(final IBlueprintNode node);

    /**
     * Returns the instant when execution started.
     * Used to calculate elapsed time and enforce timeout limits.
     *
     * @return the execution start instant
     */
    Instant getStartTime();

    /**
     * Returns the number of nodes executed so far in this execution.
     * Used to enforce the maximum node execution limit.
     *
     * @return the number of nodes executed
     */
    int getNodesExecuted();

    /**
     * Increments the counter tracking how many nodes have been executed.
     * This method is called internally by the blueprint executor.
     */
    void incrementNodes();

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
    int getMaxNodes();

    /**
     * Checks whether execution should stop due to timeout or node limit.
     * This method is called before executing each node.
     *
     * @return true if execution should stop, false otherwise
     */
    boolean isStoppingNeeded();
}
