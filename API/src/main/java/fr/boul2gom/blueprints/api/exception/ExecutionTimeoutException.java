package fr.boul2gom.blueprints.api.exception;

// Exception thrown when execution exceeds time limit
public class ExecutionTimeoutException extends ExecutionException {

    private final long execution_time;
    private final long max_time;

    public ExecutionTimeoutException(long execution_time, long max_time) {
        super(String.format("Execution exceeded time limit: %dms > %dms", execution_time, max_time));
        this.execution_time = execution_time;
        this.max_time = max_time;
    }

    public long getExecutionTime() {
        return this.execution_time;
    }

    public long getMaxTime() {
        return this.max_time;
    }
}
