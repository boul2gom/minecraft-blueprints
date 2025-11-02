package fr.boul2gom.blueprints.api.node;

@FunctionalInterface
public interface NodeFactory {

    IBlueprintNode create(NodePosition position);
}
