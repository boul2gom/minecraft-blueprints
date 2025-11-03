package fr.boul2gom.blueprints.api.execution;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;

public record ExecutionResult(Type type, String error, Duration execution_time, int nodes_executed) implements IExecutionResult {

    public ExecutionResult(Type type, @Nullable String error, Duration execution_time, int nodes_executed) {
        Objects.requireNonNull(type, "Execution result type may not be null");

        this.type = type;
        this.error = error;
        this.execution_time = execution_time;
        this.nodes_executed = nodes_executed;
    }

    public static ExecutionResult success(Duration execution_time, int nodes_executed) {
        return new ExecutionResult(Type.SUCCESS, null, execution_time, nodes_executed);
    }

    public static ExecutionResult error(String error, Duration execution_time, int nodes_executed) {
        Objects.requireNonNull(error, "Error message may not be null");
        return new ExecutionResult(Type.ERROR, error, execution_time, nodes_executed);
    }

    public static ExecutionResult timeout(Duration execution_time, int nodes_executed) {
        return new ExecutionResult(Type.TIMEOUT, "Execution exceeded time limit", execution_time, nodes_executed);
    }

    public static ExecutionResult node_limit_exceeded(Duration execution_time, int nodes_executed) {
        return new ExecutionResult(Type.NODE_LIMIT_EXCEEDED, "Execution exceeded node limit", execution_time, nodes_executed);
    }

    public static ExecutionResult validation(String error) {
        Objects.requireNonNull(error, "Error message may not be null");
        return new ExecutionResult(Type.VALIDATION_FAILED, error, Duration.ZERO, 0);
    }

    @Override
    public boolean isSuccess() {
        return this.type == Type.SUCCESS;
    }

    @Override
    public @NotNull String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
