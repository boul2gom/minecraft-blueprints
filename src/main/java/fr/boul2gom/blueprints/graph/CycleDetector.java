package fr.boul2gom.blueprints.graph;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.CycleDetectedException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.util.NodeUtils;

import java.util.*;

public class CycleDetector {

    private final IBlueprintGraph graph;

    public CycleDetector(IBlueprintGraph graph) {
        Objects.requireNonNull(graph, "Graph may not be null");
        this.graph = graph;
    }

    // Detect cycles in the execution flow graph using DFS
    public void detectCycles() {
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
        // Mark current node as visited and add to recursion stack
        visited.add(node);
        recursion_stack.add(node);
        current_path.add(node);

        // Get all execution output pins from this node using utility method
        final List<? extends IBlueprintPin> exec_outputs = NodeUtils.getExecutionOutputs(node);

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
        current_path.remove(current_path.size() - 1);
    }
}
