package fr.boul2gom.blueprints.api.execution.debug;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The IExecutionLogger interface provides a system for recording and analyzing
 * blueprint execution at a granular level. It captures detailed information about
 * each node execution, including timing, variable state, and errors.
 *
 * Primary Responsibilities:
 * - Record each node execution as a discrete step
 * - Track execution timing for performance profiling
 * - Capture variable state changes throughout execution
 * - Provide access to execution history for debugging
 * - Support enabling/disabling logging for performance
 */
public interface IExecutionLogger {

    /**
     * Returns whether logging is currently enabled.
     * When disabled, logging operations should be no-ops for performance.
     *
     * @return true if logging is enabled
     */
    boolean isEnabled();

    /**
     * Enables or disables logging.
     *
     * @param enabled whether to enable logging
     */
    void setEnabled(boolean enabled);

    /**
     * Records the start of a node execution.
     * This should be called immediately before executing a node.
     *
     * @param node the node about to execute
     * @param variablesBefore snapshot of variables before execution
     * @return the start instant, to be passed to logNodeEnd
     */
    Instant log_start(IBlueprintNode node, Map<String, Object> variablesBefore);

    /**
     * Records the end of a node execution.
     * This should be called immediately after executing a node.
     *
     * @param node the node that executed
     * @param startTime the instant returned by logNodeStart
     * @param variablesAfter snapshot of variables after execution
     * @param success whether execution was successful
     * @param error error message if execution failed, null otherwise
     */
    void log_end(
            IBlueprintNode node,
            Instant startTime,
            Map<String, Object> variablesAfter,
            boolean success,
            String error
    );

    /**
     * Returns all execution steps recorded so far, in order.
     *
     * @return immutable list of execution steps
     */
    List<IExecutionStep> getSteps();

    /**
     * Returns the total number of steps recorded.
     *
     * @return the step count
     */
    int get_step_count();

    /**
     * Returns the total execution time across all recorded steps.
     *
     * @return the cumulative execution time
     */
    Duration get_total_execution_time();

    /**
     * Returns statistics about execution time per node type.
     * Useful for identifying performance bottlenecks.
     *
     * @return map of node IDs to total execution time
     */
    Map<String, Duration> get_execution_time_by_node();

    /**
     * Returns the slowest nodes by execution time.
     *
     * @param limit maximum number of nodes to return
     * @return list of node IDs sorted by execution time (slowest first)
     */
    List<String> get_slowest_nodes(int limit);

    /**
     * Clears all recorded execution steps.
     * Should be called at the start of each blueprint execution.
     */
    void clear();

    /**
     * Returns whether any errors occurred during execution.
     *
     * @return true if at least one step failed
     */
    boolean hasErrors();

    /**
     * Returns only the steps that failed.
     *
     * @return list of failed execution steps
     */
    List<IExecutionStep> get_failed_steps();

    /**
     * Logs a summary of the execution to the console.
     * This provides a concise overview of what happened during execution.
     */
    void summary();
}
