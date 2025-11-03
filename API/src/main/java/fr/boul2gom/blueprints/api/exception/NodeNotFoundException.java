package fr.boul2gom.blueprints.api.exception;

// Exception thrown when a node is not found in the graph
public class NodeNotFoundException extends BlueprintException {

    private final String node_id;

    public NodeNotFoundException(String node_id) {
        super("Node not found: " + node_id);
        this.node_id = node_id;
    }

    public String getNodeId() {
        return this.node_id;
    }
}
