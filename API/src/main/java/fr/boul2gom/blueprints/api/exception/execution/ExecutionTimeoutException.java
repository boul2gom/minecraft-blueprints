package fr.boul2gom.blueprints.api.exception.execution;

/**
 * Represents an exception that occurs when the execution of a process exceeds the allowed time limit.
 * This exception provides detailed information about the execution time and the maximum allowed time.
 */
public class ExecutionTimeoutException extends ExecutionException {

    /** The actual execution time in milliseconds. */
    private final long execution_time;
    /** The maximum allowed execution time in milliseconds. */
    private final long max_time;

    /**
     * Constructs a new {@code ExecutionTimeoutException}. This exception indicates that the process execution
     * exceeded the allowed time limit.
     *
     * @param execution_time the actual execution time in milliseconds.
     * @param max_time the maximum allowed execution time in milliseconds.
     */
    public ExecutionTimeoutException(long execution_time, long max_time) {
        // Java 15+: Use formatted() instead of String.format()
        super("Execution exceeded time limit: %dms > %dms".formatted(execution_time, max_time));
        this.execution_time = execution_time;
        this.max_time = max_time;
    }

    /**
     * Gets the actual execution time in milliseconds.
     * @return the actual execution time in milliseconds.
     */
    public long get_execution_time() {
        return this.execution_time;
    }

    /**
     * Gets the maximum allowed execution time in milliseconds.
     * @return the maximum allowed execution time in milliseconds.
     */
    public long get_max_time() {
        return this.max_time;
    }
}
