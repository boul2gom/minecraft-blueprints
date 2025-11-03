package fr.boul2gom.blueprints.api.exception;

// Exception thrown when execution exceeds node limit
public class NodeLimitExceededException extends ExecutionException {

    private final int nodes_executed;
    private final int max_nodes;

    public NodeLimitExceededException(int nodes_executed, int max_nodes) {
        super(String.format("Execution exceeded node limit: %d > %d", nodes_executed, max_nodes));
        this.nodes_executed = nodes_executed;
        this.max_nodes = max_nodes;
    }

    public int getNodesExecuted() {
        return this.nodes_executed;
    }

    public int getMaxNodes() {
        return this.max_nodes;
    }
}
