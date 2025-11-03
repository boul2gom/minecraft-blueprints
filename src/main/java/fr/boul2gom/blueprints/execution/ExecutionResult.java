package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExecutionResult implements IExecutionResult {

    private final ExecutionResultType type;
    private final String error;
    private final long execution_time;
    private final int nodes_executed;

    private ExecutionResult(ExecutionResultType type, @Nullable String error, long execution_time, int nodes_executed) {
        Objects.requireNonNull(type, "Execution result type may not be null");

        this.type = type;
        this.error = error;
        this.execution_time = execution_time;
        this.nodes_executed = nodes_executed;
    }

    // Factory method for successful execution
    public static ExecutionResult success(long execution_time, int nodes_executed) {
        return new ExecutionResult(ExecutionResultType.SUCCESS, null, execution_time, nodes_executed);
    }

    // Factory method for execution error
    public static ExecutionResult error(String error, long execution_time, int nodes_executed) {
        Objects.requireNonNull(error, "Error message may not be null");
        return new ExecutionResult(ExecutionResultType.ERROR, error, execution_time, nodes_executed);
    }

    // Factory method for timeout
    public static ExecutionResult timeout(long execution_time, int nodes_executed) {
        return new ExecutionResult(ExecutionResultType.TIMEOUT, "Execution exceeded time limit", execution_time, nodes_executed);
    }

    // Factory method for node limit exceeded
    public static ExecutionResult nodeLimitExceeded(long execution_time, int nodes_executed) {
        return new ExecutionResult(ExecutionResultType.NODE_LIMIT_EXCEEDED, "Execution exceeded node limit", execution_time, nodes_executed);
    }

    // Factory method for validation failure
    public static ExecutionResult validationFailed(String error) {
        Objects.requireNonNull(error, "Error message may not be null");
        return new ExecutionResult(ExecutionResultType.VALIDATION_FAILED, error, 0, 0);
    }

    @Override
    public boolean isSuccess() {
        return this.type == ExecutionResultType.SUCCESS;
    }

    @Override
    @Nullable
    public String getError() {
        return this.error;
    }

    @Override
    public long getExecutionTime() {
        return this.execution_time;
    }

    @Override
    public int getNodesExecuted() {
        return this.nodes_executed;
    }

    @Override
    public ExecutionResultType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
