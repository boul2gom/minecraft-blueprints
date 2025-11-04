package fr.boul2gom.blueprints.api.exception.execution;


/**
 * This exception is thrown when the execution of a process exceeds the allowed node limit.
 * It provides information about the actual number of executed nodes and the maximum allowable nodes.
 */
public class NodeLimitExceededException extends ExecutionException {

    /** The actual number of executed nodes. */
    private final int nodes_executed;
    /** The maximum allowable number of nodes. */
    private final int max_nodes;

    /**
     * Constructs a new {@code NodeLimitExceededException}. This exception indicates that the process execution
     * exceeded the allowed node limit.
     * @param nodes_executed the actual number of executed nodes.
     * @param max_nodes the maximum allowable number of nodes.
     */
    public NodeLimitExceededException(int nodes_executed, int max_nodes) {
        // Java 15+: Use formatted() instead of String.format()
        super("Execution exceeded node limit: %d > %d".formatted(nodes_executed, max_nodes));
        this.nodes_executed = nodes_executed;
        this.max_nodes = max_nodes;
    }

    /**
     * Gets the actual number of executed nodes.
     * @return the actual number of executed nodes.
     */
    public int getNodesExecuted() {
        return this.nodes_executed;
    }

    /**
     * Gets the maximum allowable number of nodes.
     * @return the maximum allowable number of nodes.
     */
    public int getMaxNodes() {
        return this.max_nodes;
    }
}
