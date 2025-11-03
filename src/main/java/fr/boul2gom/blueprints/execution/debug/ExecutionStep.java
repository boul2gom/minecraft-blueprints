package fr.boul2gom.blueprints.execution.debug;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionStep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record ExecutionStep(
        int index,
        String nodeId,
        String nodeName,
        Instant startTime,
        Duration executionTime,
        Map<String, Object> variablesBefore,
        Map<String, Object> variablesAfter,
        boolean success,
        @Nullable String error
) implements IExecutionStep {

    public ExecutionStep {
        Objects.requireNonNull(nodeId, "Node ID may not be null");
        Objects.requireNonNull(nodeName, "Node name may not be null");
        Objects.requireNonNull(startTime, "Start time may not be null");
        Objects.requireNonNull(executionTime, "Execution time may not be null");
        Objects.requireNonNull(variablesBefore, "Variables before may not be null");
        Objects.requireNonNull(variablesAfter, "Variables after may not be null");

        if (index < 0) {
            throw new IllegalArgumentException("Step index must be non-negative");
        }

        // Variables immuables
        variablesBefore = Collections.unmodifiableMap(variablesBefore);
        variablesAfter = Collections.unmodifiableMap(variablesAfter);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public String getNodeName() {
        return this.nodeName;
    }

    @Override
    public Instant getStartTime() {
        return this.startTime;
    }

    @Override
    public Duration getExecutionTime() {
        return this.executionTime;
    }

    @Override
    public Map<String, Object> getVariablesBefore() {
        return this.variablesBefore;
    }

    @Override
    public Map<String, Object> getVariablesAfter() {
        return this.variablesAfter;
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    @Nullable
    public String getError() {
        return this.error;
    }

    @Override
    public @NotNull String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
