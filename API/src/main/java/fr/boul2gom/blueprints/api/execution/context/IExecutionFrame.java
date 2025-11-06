package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.Optional;

/**
 * Represents an execution frame with isolated pin value cache.
 * Each frame maintains its own cache to prevent cross-iteration pollution.
 *
 * Frames are organized in a stack:
 * - Root frame: Main blueprint execution
 * - Child frames: Loop iterations, nested executions
 *
 * When a loop executes:
 * 1. Push new frame before loop body
 * 2. Execute loop body (cache is isolated)
 * 3. Pop frame after loop body (cache is discarded)
 */
public interface IExecutionFrame {

    /**
     * Gets the cached value for a pin in this frame.
     *
     * @param pin The pin to get the value for
     * @return Optional containing the cached value if present
     */
    Optional<Object> get_pin_value(IBlueprintPin pin);

    /**
     * Sets the cached value for a pin in this frame.
     *
     * @param pin The pin to cache the value for
     * @param value The value to cache
     */
    void set_pin_value(IBlueprintPin pin, Object value);

    /**
     * Checks if a pin value is cached in this frame.
     *
     * @param pin The pin to check
     * @return true if the pin has a cached value
     */
    boolean has_pin_value(IBlueprintPin pin);

    /**
     * Invalidates cached values for all pins of a specific node.
     *
     * @param node The node whose pins should be invalidated
     */
    void invalidate_node(IBlueprintNode node);

    /**
     * Clears all cached pin values in this frame.
     */
    void clear();

    /**
     * Gets the depth of this frame in the stack.
     * Root frame has depth 0, first child has depth 1, etc.
     *
     * @return The frame depth
     */
    int getDepth();

    /**
     * Gets the node that created this frame (e.g., ForLoopNode).
     * Null for the root frame.
     *
     * @return Optional containing the owner node
     */
    Optional<IBlueprintNode> getOwner();

    /**
     * Creates a child frame for nested execution.
     *
     * @param owner The node creating the child frame
     * @return A new child frame
     */
    IExecutionFrame create_child(IBlueprintNode owner);
}
