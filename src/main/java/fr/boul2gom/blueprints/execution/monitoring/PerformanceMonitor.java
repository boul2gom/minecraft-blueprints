package fr.boul2gom.blueprints.execution.monitoring;

import fr.boul2gom.blueprints.api.exception.execution.ExecutionTimeoutException;
import fr.boul2gom.blueprints.api.exception.execution.NodeLimitExceededException;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record PerformanceMonitor(Duration max_execution_time, int max_nodes_per_execution) {

    public PerformanceMonitor {
        if (max_execution_time == null || max_execution_time.isZero() || max_execution_time.isNegative()) {
            throw new IllegalArgumentException("Max execution time must be positive");
        }
        if (max_nodes_per_execution <= 0) {
            throw new IllegalArgumentException("Max nodes per execution must be positive");
        }
    }

    /**
     * Checks the execution context against the performance limits.
     * Throws an exception if any limit is exceeded.
     * @param context the execution context to check.
     */
    public void check(IExecutionContext context) {
        Objects.requireNonNull(context, "Execution context may not be null");

        // Check execution time
        final Duration elapsed_time = Duration.between(context.get_start_time(), Instant.now());
        if (elapsed_time.compareTo(this.max_execution_time) > 0) {
            throw new ExecutionTimeoutException(elapsed_time.toMillis(), this.max_execution_time.toMillis());
        }

        // Check node limit
        if (context.get_nodes_executed() >= this.max_nodes_per_execution) {
            throw new NodeLimitExceededException(
                    context.get_nodes_executed(),
                    this.max_nodes_per_execution
            );
        }
    }
}
