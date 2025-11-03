package fr.boul2gom.blueprints.api.execution.debug;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Represents a single step in the execution of a blueprint graph.
 * Each step captures the state and timing information for one node execution.
 *
 * Primary Responsibilities:
 * - Record which node was executed and when
 * - Track execution timing for performance analysis
 * - Capture variable state before and after execution
 * - Store execution result (success/error)
 */
public interface IExecutionStep {

    /**
     * Returns the unique identifier of this execution step.
     * Steps are numbered sequentially starting from 0.
     *
     * @return the step index
     */
    int index();

    /**
     * Returns the ID of the node that was executed.
     *
     * @return the node ID
     */
    String node_id();

    /**
     * Returns the name of the node that was executed.
     *
     * @return the node name
     */
    String node_name();

    /**
     * Returns the instant when this node started executing.
     *
     * @return the start instant
     */
    Instant start_time();

    /**
     * Returns how long this node took to execute.
     *
     * @return the execution duration
     */
    Duration execution_time();

    /**
     * Returns an immutable snapshot of all variables before this node executed.
     *
     * @return map of variable names to values
     */
    Map<String, Object> variables_before();

    /**
     * Returns an immutable snapshot of all variables after this node executed.
     *
     * @return map of variable names to values
     */
    Map<String, Object> variables_after();

    /**
     * Returns whether this node executed successfully.
     *
     * @return true if successful, false if an error occurred
     */
    boolean success();

    /**
     * Returns the error message if execution failed.
     *
     * @return the error message, or null if successful
     */
    @Nullable
    String error();
}
