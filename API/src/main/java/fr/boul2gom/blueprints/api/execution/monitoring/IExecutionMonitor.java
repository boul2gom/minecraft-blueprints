package fr.boul2gom.blueprints.api.execution.monitoring;

import java.time.Duration;
import java.time.Instant;

/**
 * Provider for monitoring blueprint execution and enforcing safety limits.
 * Handles timeout tracking, node count limits, and execution statistics.
 *
 * Safety limits (default):
 * - Max execution time: 50ms (one server tick)
 * - Max nodes executed: 10,000 per invocation
 */
public interface IExecutionMonitor {

    /**
     * Marks the start of blueprint execution.
     * Should be called once at the beginning of execution.
     */
    void start();

    /**
     * Gets the instant when execution started.
     *
     * @return The start instant
     */
    Instant get_start_time();

    /**
     * Gets the elapsed execution time in milliseconds.
     *
     * @return The elapsed time in ms
     */
    Duration get_elapsed_time();

    /**
     * Checks if execution has exceeded the maximum allowed time.
     *
     * @param maxMs The maximum allowed time in milliseconds
     * @return true if execution has timed out
     */
    boolean has_timed_out(long maxMs);

    /**
     * Increments the count of executed nodes.
     * Should be called after each node execution.
     */
    void increment_node_count();

    /**
     * Gets the total number of nodes executed so far.
     *
     * @return The node execution count
     */
    int get_node_count();

    /**
     * Checks if the number of executed nodes exceeds the limit.
     *
     * @param maxNodes The maximum allowed node count
     * @return true if the limit has been exceeded
     */
    boolean has_exceeded_node_limit(int maxNodes);

    /**
     * Gets a summary of execution statistics.
     *
     * @return A formatted string with execution stats
     */
    String getStatistics();

    /**
     * Resets all monitoring data for a new execution.
     */
    void reset();
}
