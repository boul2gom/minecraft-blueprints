package fr.boul2gom.blueprints.util;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for common node operations to reduce code duplication.
 */
public final class NodeUtils {

    private NodeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get all execution flow output pins from a node.
     * This is a common operation used in execution traversal and cycle detection.
     *
     * @param node The node to get execution outputs from
     * @return List of execution flow output pins
     */
    public static List<? extends IBlueprintPin> getExecutionOutputs(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");
        
        return node.getOutputs().stream()
            .filter(pin -> pin.getType() == PinType.EXECUTION_FLOW)
            .toList();
    }

    /**
     * Get all execution flow input pins from a node.
     * This is used to determine entry points in the graph.
     *
     * @param node The node to get execution inputs from
     * @return List of execution flow input pins
     */
    public static List<? extends IBlueprintPin> getExecutionInputs(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");
        
        return node.getInputs().stream()
            .filter(pin -> pin.getType() == PinType.EXECUTION_FLOW)
            .toList();
    }
}
