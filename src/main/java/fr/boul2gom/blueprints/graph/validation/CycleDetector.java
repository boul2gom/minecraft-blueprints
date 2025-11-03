package fr.boul2gom.blueprints.graph.validation;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.validation.CycleDetectedException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.*;

public record CycleDetector(IBlueprintGraph graph) {

    public CycleDetector {
        Objects.requireNonNull(graph, "Graph may not be null");
    }

    // Detect cycles in the execution flow graph using DFS
    public void detect() {
        final Set<IBlueprintNode> visited = new HashSet<>();
        final Set<IBlueprintNode> recursion_stack = new HashSet<>();
        final List<IBlueprintNode> current_path = new ArrayList<>();

        // Check each node as a potential starting point
        for (final IBlueprintNode node : this.graph.getNodes()) {
            if (!visited.contains(node)) {
                this.dfs(node, visited, recursion_stack, current_path);
            }
        }
    }

    // Depth-First Search to detect cycles
    private void dfs(
            IBlueprintNode node,
            Set<IBlueprintNode> visited,
            Set<IBlueprintNode> recursion_stack,
            List<IBlueprintNode> current_path
    ) {
        visited.add(node);
        recursion_stack.add(node);
        current_path.add(node);

        // Get all execution output pins from this node
        final List<? extends IBlueprintPin> exec_outputs = node.getOutputs().stream()
                .filter(IBlueprintPin::isExecution)
                .toList();

        // Follow each execution output connection
        for (final IBlueprintPin output_pin : exec_outputs) {
            for (final IBlueprintConnection connection : output_pin.getConnections()) {
                // Get the connected input pin and its node
                final IBlueprintPin input_pin = connection.getOther(output_pin);
                final IBlueprintNode next_node = input_pin.getNode();

                // If next node is in recursion stack, we found a cycle
                if (recursion_stack.contains(next_node)) {
                    // Build cycle path from current_path
                    final List<IBlueprintNode> cycle_path = new ArrayList<>();
                    boolean found_start = false;
                    for (final IBlueprintNode path_node : current_path) {
                        if (path_node.equals(next_node)) {
                            found_start = true;
                        }
                        if (found_start) {
                            cycle_path.add(path_node);
                        }
                    }
                    cycle_path.add(next_node); // Complete the cycle

                    throw new CycleDetectedException(cycle_path);
                }

                // If next node hasn't been visited, continue DFS
                if (!visited.contains(next_node)) {
                    this.dfs(next_node, visited, recursion_stack, current_path);
                }
            }
        }

        // Remove node from recursion stack when backtracking
        recursion_stack.remove(node);
        current_path.removeLast();
    }
}
