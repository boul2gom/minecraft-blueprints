package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;

/**
 * Provider for tracking node iterations during blueprint execution.
 * Handles loop iteration counting and nested loop management.
 *
 * Iteration semantics: 0-based indexing
 * - First execution: getIndex() returns 0
 * - Second execution: getIndex() returns 1
 * - etc.
 */
public interface IIterationTracker {

    /**
     * Increments the iteration count for a specific node.
     * Should be called each time a node is executed in a loop.
     *
     * @param node The node being iterated
     */
    void increment(IBlueprintNode node);

    /**
     * Gets the current iteration index for a node (0-based).
     * Returns 0 for the first execution, 1 for the second, etc.
     *
     * @param node The node to get the iteration index for
     * @return The 0-based iteration index
     */
    int getIndex(IBlueprintNode node);

    /**
     * Gets the total number of times a node has been executed.
     * Equivalent to getIndex() + 1.
     *
     * @param node The node to get the count for
     * @return The total execution count
     */
    int getCount(IBlueprintNode node);

    /**
     * Resets the iteration count for a specific node.
     * Called when exiting a loop scope.
     *
     * @param node The node to reset
     */
    void reset(IBlueprintNode node);

    /**
     * Checks if a node has exceeded the maximum allowed iterations.
     *
     * @param node The node to check
     * @param maxIterations The maximum allowed iterations
     * @return true if the node has exceeded the limit
     */
    boolean hasExceeded(IBlueprintNode node, int maxIterations);

    /**
     * Clears all iteration tracking data.
     * Called when starting a new execution context.
     */
    void clear();
}
