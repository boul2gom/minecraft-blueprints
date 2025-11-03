package fr.boul2gom.blueprints.graph.validation;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.Objects;

public class GraphValidator {

    private final IBlueprintGraph graph;
    private final CycleDetector cycle_detector;

    public GraphValidator(IBlueprintGraph graph) {
        Objects.requireNonNull(graph, "Graph may not be null");

        this.graph = graph;
        this.cycle_detector = new CycleDetector(graph);
    }

    // Validate the entire graph
    public void validate() {
        // 1. Check for cycles in execution flow
        this.cycle_detector.detect();

        // 2. Validate each node individually
        for (final IBlueprintNode node : this.graph.getNodes()) {
            try {
                node.validate();
            } catch (IllegalStateException e) {
                throw new ValidationException(
                    String.format("Node '%s' validation failed: %s", node.getName(), e.getMessage()),
                    e
                );
            }
        }

        // 3. Check for orphaned nodes (nodes with no connections) - warning only
        final long orphaned_count = this.graph.getNodes().stream()
            .filter(node -> {
                final boolean has_input_connections = node.getInputs().stream()
                    .anyMatch(pin -> !pin.getConnections().isEmpty());
                final boolean has_output_connections = node.getOutputs().stream()
                    .anyMatch(pin -> !pin.getConnections().isEmpty());
                return !has_input_connections && !has_output_connections;
            })
            .count();

        if (orphaned_count > 0) {
            // Log warning but don't fail validation
            // TODO: Add proper logging when logging system is implemented
            MinecraftBlueprints.LOGGER.info("Warning: Graph contains {} orphaned node(s)", orphaned_count);
        }

        // 4. Check if graph has entry points
        if (this.graph.get_entry_points().isEmpty()) {
            throw new ValidationException("Graph has no entry points (nodes with unconnected execution inputs)");
        }
    }
}
