package fr.boul2gom.blueprints.api.exception;

/**
 * Exception thrown when a specific node cannot be found in a blueprint graph.
 *
 * This exception is a specialized form of {@code BlueprintException}, providing
 * contextual information about the missing node, including its unique identifier.
 *
 * This is typically used in scenarios where an operation expects a node to be
 * present in the graph, such as when resolving connections or retrieving node
 * metadata, but the required node is not available.
 */
public class NodeNotFoundException extends BlueprintException {

    /** The unique identifier of the missing node. */
    private final String node_id;

    /**
     * Constructs a new {@code NodeNotFoundException} with the specified missing node identifier.
     * @param node_id the unique identifier of the missing node.
     */
    public NodeNotFoundException(String node_id) {
        super("Node not found: " + node_id);
        this.node_id = node_id;
    }

    /**
     * Gets the unique identifier of the missing node.
     * @return the unique identifier of the missing node.
     */
    public String getNode() {
        return this.node_id;
    }
}
