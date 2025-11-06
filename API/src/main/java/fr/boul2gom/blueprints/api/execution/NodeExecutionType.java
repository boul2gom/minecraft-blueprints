package fr.boul2gom.blueprints.api.execution;

/**
 * Classification of nodes based on their execution flow topology.
 * Used by the DAG execution planner to determine parallel vs sequential execution.
 */
public enum NodeExecutionType {
    /**
     * Sequential node: Single execution output, executes next node in sequence.
     * Example: Action nodes with one exec output pin
     */
    SEQUENTIAL,

    /**
     * Fork node: Multiple execution outputs, creates parallel execution branches.
     * Example: Branch node with "true" and "false" outputs
     */
    FORK,

    /**
     * Join node: Multiple execution inputs, synchronization point for parallel branches.
     * Execution waits until ALL incoming branches complete before proceeding.
     * Example: Node with multiple exec input connections
     */
    JOIN,

    /**
     * Entry node: No execution inputs, graph entry point.
     * Example: Event nodes that start execution
     */
    ENTRY
}
