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
        String node_id,
        String node_name,
        Instant start_time,
        Duration execution_time,
        Map<String, Object> variables_before,
        Map<String, Object> variables_after,
        boolean success,
        @Nullable String error
) implements IExecutionStep {

    public ExecutionStep {
        Objects.requireNonNull(node_id, "Node ID may not be null");
        Objects.requireNonNull(node_name, "Node name may not be null");
        Objects.requireNonNull(start_time, "Start time may not be null");
        Objects.requireNonNull(execution_time, "Execution time may not be null");
        Objects.requireNonNull(variables_before, "Variables before may not be null");
        Objects.requireNonNull(variables_after, "Variables after may not be null");

        if (index < 0) {
            throw new IllegalArgumentException("Step index must be non-negative");
        }

        // Variables immuables
        variables_before = Collections.unmodifiableMap(variables_before);
        variables_after = Collections.unmodifiableMap(variables_after);
    }

    @Override
    public @NotNull String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
