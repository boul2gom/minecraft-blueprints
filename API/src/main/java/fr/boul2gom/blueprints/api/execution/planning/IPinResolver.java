package fr.boul2gom.blueprints.api.execution.planning;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.Optional;

/**
 * Provider for resolving pin values during blueprint execution.
 * Handles pin value caching, pure node evaluation, and data flow.
 */
public interface IPinResolver {

    /**
     * Gets the value of a pin, evaluating the source node if necessary.
     * For input pins, this will trace back to the connected output pin.
     * For pure nodes (no execution pins), this will execute the node on-demand.
     *
     * @param pin The pin to get the value for
     * @return Optional containing the pin value if available
     */
    Optional<Object> resolve(IBlueprintPin pin);

    /**
     * Sets the value of a pin directly.
     * Used by nodes to set their output pin values after execution.
     *
     * @param pin The pin to set the value for
     * @param value The value to set
     */
    void set(IBlueprintPin pin, Object value);

    /**
     * Checks if a node is pure (has no execution pins).
     * Pure nodes are evaluated on-demand when their output is needed.
     *
     * @param node The node to check
     * @return true if the node has no execution flow pins
     */
    boolean isPure(IBlueprintNode node);

    /**
     * Invalidates cached values for all pins belonging to a specific node.
     * Called before re-executing a node to ensure fresh evaluation.
     *
     * @param node The node whose pin values should be invalidated
     */
    void invalidate(IBlueprintNode node);

    /**
     * Clears all cached pin values.
     * Called when starting a new execution or entering a new frame.
     */
    void clear_cache();
}
