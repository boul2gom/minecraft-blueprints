package fr.boul2gom.blueprints.api.node;

/**
 * The NodeFactory interface is a functional interface used for creating instances
 * of {@link IBlueprintNode}. It provides a single method that takes a {@link NodePosition}
 * as input and returns an instance of a blueprint node.
 *
 * The factory pattern encapsulates the logic of node creation,
 * ensuring that nodes are created with valid configurations.
 */
@FunctionalInterface
public interface NodeFactory {

    /**
     * Creates a new instance of {@link IBlueprintNode}.
     * @param position the position of the node within the blueprint graph.
     * @return a new instance of a blueprint node.
     */
    IBlueprintNode create(NodePosition position);
}
