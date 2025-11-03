package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.api.exception.ExecutionTimeoutException;
import fr.boul2gom.blueprints.api.exception.NodeLimitExceededException;
import fr.boul2gom.blueprints.api.execution.IExecutionContext;

import java.util.Objects;

public class PerformanceMonitor {

    private final long max_execution_time;
    private final int max_nodes_per_execution;

    public PerformanceMonitor(long max_execution_time, int max_nodes_per_execution) {
        if (max_execution_time <= 0) {
            throw new IllegalArgumentException("Max execution time must be positive");
        }
        if (max_nodes_per_execution <= 0) {
            throw new IllegalArgumentException("Max nodes per execution must be positive");
        }

        this.max_execution_time = max_execution_time;
        this.max_nodes_per_execution = max_nodes_per_execution;
    }

    // Check if execution should stop due to timeout or node limit
    public void checkLimits(IExecutionContext context) {
        Objects.requireNonNull(context, "Execution context may not be null");

        // Check node limit first (cheaper operation)
        if (context.getNodesExecuted() >= this.max_nodes_per_execution) {
            throw new NodeLimitExceededException(
                context.getNodesExecuted(),
                this.max_nodes_per_execution
            );
        }

        // Check execution time (calculate once)
        final long elapsed_time = System.currentTimeMillis() - context.getStartTime();
        if (elapsed_time > this.max_execution_time) {
            throw new ExecutionTimeoutException(elapsed_time, this.max_execution_time);
        }
    }

    public long getMaxExecutionTime() {
        return this.max_execution_time;
    }

    public int getMaxNodesPerExecution() {
        return this.max_nodes_per_execution;
    }
}
