package fr.boul2gom.blueprints.execution.monitoring;

import fr.boul2gom.blueprints.api.execution.monitoring.IExecutionMonitor;

import java.time.Duration;
import java.time.Instant;

/**
 * Provider implementation for monitoring execution and enforcing safety limits.
 * Tracks execution time, node count, and provides execution statistics.
 */
public class ExecutionMonitor implements IExecutionMonitor {

    private Instant start_time;
    private int node_count;

    public ExecutionMonitor() {
        this.start_time = null;
        this.node_count = 0;
    }

    @Override
    public void start() {
        this.start_time = Instant.now();
        this.node_count = 0;
    }

    @Override
    public Instant get_start_time() {
        return this.start_time != null ? this.start_time : Instant.now();
    }

    @Override
    public Duration get_elapsed_time() {
        if (this.start_time == null) {
            return Duration.ZERO;
        }
        return Duration.between(this.start_time, Instant.now());
    }

    @Override
    public boolean has_timed_out(long maxMs) {
        final long elapsed = this.get_elapsed_time().toMillis();
        return elapsed >= maxMs;
    }

    @Override
    public void increment_node_count() {
        this.node_count++;
    }

    @Override
    public int get_node_count() {
        return this.node_count;
    }

    @Override
    public boolean has_exceeded_node_limit(int maxNodes) {
        return this.node_count >= maxNodes;
    }

    @Override
    public String getStatistics() {
        return String.format("Execution: %d nodes in %dms", this.node_count, this.get_elapsed_time().toMillis());
    }

    @Override
    public void reset() {
        this.start_time = null;
        this.node_count = 0;
    }

    @Override
    public String toString() {
        return String.format("ExecutionMonitor(nodes=%d, elapsed=%dms)",
            this.node_count,
            this.get_elapsed_time().toMillis());
    }
}
