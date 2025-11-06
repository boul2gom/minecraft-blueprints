package fr.boul2gom.blueprints.execution.debug;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionLogger;
import fr.boul2gom.blueprints.api.execution.debug.IExecutionStep;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExecutionLogger implements IExecutionLogger {

    private boolean enabled;
    private final List<IExecutionStep> steps;
    private final Map<String, Duration> execution_time;

    private int index;

    public ExecutionLogger() {
        this(true);
    }

    public ExecutionLogger(boolean enabled) {
        this.enabled = enabled;

        this.index = 0;
        this.steps = new ArrayList<>();
        this.execution_time = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Instant log_start(IBlueprintNode node, Map<String, Object> variablesBefore) {
        if (!this.enabled) {
            return Instant.now();
        }

        Objects.requireNonNull(node, "Node may not be null");
        Objects.requireNonNull(variablesBefore, "Variables before may not be null");

        MinecraftBlueprints.LOGGER.info(
                "[Blueprint Execution] >>> Starting Node '{}' (id: {}) - Variables: {}",
                node.getName(),
                node.getId(),
                variablesBefore.isEmpty() ? "none" : variablesBefore.size() + " variable(s)"
        );

        return Instant.now();
    }

    @Override
    public void log_end(
            IBlueprintNode node,
            Instant startTime,
            Map<String, Object> variablesAfter,
            boolean success,
            String error
    ) {
        if (!this.enabled) {
            return;
        }

        Objects.requireNonNull(node, "Node may not be null");
        Objects.requireNonNull(startTime, "Start time may not be null");
        Objects.requireNonNull(variablesAfter, "Variables after may not be null");

        final Instant endTime = Instant.now();
        final Duration executionTime = Duration.between(startTime, endTime);

        final Map<String, Object> variablesBefore = this.steps.isEmpty()
                ? Collections.emptyMap()
                : this.steps.getLast().variables_after();

        final ExecutionStep step = new ExecutionStep(
                this.index++,
                node.getId(),
                node.getName(),
                startTime,
                executionTime,
                Map.copyOf(variablesBefore),
                Map.copyOf(variablesAfter),
                success,
                error
        );

        this.steps.add(step);

        this.execution_time.merge(
                node.getId(),
                executionTime,
                Duration::plus
        );

        if (success) {
            MinecraftBlueprints.LOGGER.info(
                    "[Blueprint Execution] <<< Completed Node '{}' (id: {}) in {}ms [Step #{}] - Variables: {}",
                    node.getName(),
                    node.getId(),
                    executionTime.toMillis(),
                    step.index(),
                    variablesAfter.isEmpty() ? "none" : variablesAfter.size() + " variable(s)"
            );
        } else {
            MinecraftBlueprints.LOGGER.error(
                    "[Blueprint Execution] <<< FAILED Node '{}' (id: {}) after {}ms [Step #{}] - Error: {}",
                    node.getName(),
                    node.getId(),
                    executionTime.toMillis(),
                    step.index(),
                    error
            );
        }
    }

    @Override
    public List<IExecutionStep> getSteps() {
        return Collections.unmodifiableList(this.steps);
    }

    @Override
    public int get_step_count() {
        return this.steps.size();
    }

    @Override
    public Duration get_total_execution_time() {
        return this.steps.stream()
                .map(IExecutionStep::execution_time)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public Map<String, Duration> get_execution_time_by_node() {
        return Collections.unmodifiableMap(this.execution_time);
    }

    @Override
    public List<String> get_slowest_nodes(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        return this.execution_time.entrySet().stream()
                .sorted(Map.Entry.<String, Duration>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void clear() {
        this.steps.clear();
        this.execution_time.clear();
        this.index = 0;
    }

    @Override
    public boolean hasErrors() {
        return this.steps.stream()
                .anyMatch(step -> !step.success());
    }

    @Override
    public List<IExecutionStep> get_failed_steps() {
        return this.steps.stream()
                .filter(step -> !step.success())
                .collect(Collectors.toList());
    }

    @Override
    public void summary() {
        if (!this.enabled || this.steps.isEmpty()) {
            return;
        }

        MinecraftBlueprints.LOGGER.info("=".repeat(60));
        MinecraftBlueprints.LOGGER.info("[Blueprint Execution Summary]");
        MinecraftBlueprints.LOGGER.info("=".repeat(60));
        MinecraftBlueprints.LOGGER.info("Total steps executed: {}", this.get_step_count());
        MinecraftBlueprints.LOGGER.info("Total execution time: {}ms", this.get_total_execution_time().toMillis());
        MinecraftBlueprints.LOGGER.info("Success rate: {}/{} ({} errors)",
                this.steps.size() - this.get_failed_steps().size(),
                this.steps.size(),
                this.get_failed_steps().size()
        );

        if (!this.execution_time.isEmpty()) {
            MinecraftBlueprints.LOGGER.info("");
            MinecraftBlueprints.LOGGER.info("Top 3 slowest nodes:");
            final List<String> slowest = this.get_slowest_nodes(Math.min(3, this.execution_time.size()));
            for (int i = 0; i < slowest.size(); i++) {
                final String nodeId = slowest.get(i);
                final Duration time = this.execution_time.get(nodeId);
                MinecraftBlueprints.LOGGER.info("  {}. {} - {}ms", i + 1, nodeId, time.toMillis());
            }
        }

        if (this.hasErrors()) {
            MinecraftBlueprints.LOGGER.info("");
            MinecraftBlueprints.LOGGER.error("Failed steps:");
            for (final IExecutionStep failed_step : this.get_failed_steps()) {
                MinecraftBlueprints.LOGGER.error("  - Step #{}: {} - {}",
                        failed_step.index(),
                        failed_step.node_name(),
                        failed_step.error()
                );
            }
        }

        MinecraftBlueprints.LOGGER.info("=".repeat(60));
    }

    @Override
    public String toString() {
        return String.format("ExecutionLogger(enabled=%b, steps=%d)", this.enabled, this.steps.size());
    }
}
