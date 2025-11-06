package fr.boul2gom.blueprints.graph.validation;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.validation.CycleDetectedException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.*;

/**
 * Detects cycles in blueprint execution flow graphs using Depth-First Search (DFS).
 *
 * Algorithm Overview:
 * Uses a modified DFS with three data structures:
 * 1. visited - tracks all nodes we've explored (prevents re-exploration)
 * 2. recursion_stack - tracks nodes in the current DFS path (detects back edges)
 * 3. current_path - tracks the actual path for cycle reconstruction
 *
 * Key Insight:
 * A cycle exists if we encounter a node that's in the recursion_stack.
 * This means we've found a "back edge" pointing to an ancestor in the DFS tree.
 *
 * Time Complexity: O(V + E) where V = nodes, E = execution flow connections
 * Space Complexity: O(V) for the data structures
 */
public record CycleDetector(IBlueprintGraph graph) {

    public CycleDetector {
        Objects.requireNonNull(graph, "Graph may not be null");
    }

    /**
     * Detects cycles in the execution flow graph using DFS.
     * Throws CycleDetectedException if a cycle is found.
     *
     * The algorithm only considers EXECUTION_FLOW connections, as data flow
     * connections cannot create infinite execution loops.
     *
     * @throws CycleDetectedException if a cycle is detected
     */
    public void detect() {
        // visited: nodes we've fully explored (marked when DFS completes for that subtree)
        final Set<IBlueprintNode> visited = new HashSet<>();

        // recursion_stack: nodes currently in the DFS call stack
        // If we reach a node already in this stack, we've found a cycle
        final Set<IBlueprintNode> recursion_stack = new HashSet<>();

        // current_path: ordered list of nodes in current DFS path
        // Used to reconstruct the cycle path when a cycle is detected
        final List<IBlueprintNode> current_path = new ArrayList<>();

        // Start DFS from each unvisited node (handles disconnected graphs)
        for (final IBlueprintNode node : this.graph.getNodes()) {
            if (!visited.contains(node)) {
                this.dfs(node, visited, recursion_stack, current_path);
            }
        }
    }

    /**
     * Recursive DFS helper that explores the graph and detects cycles.
     *
     * DFS State Transitions:
     * 1. Enter node: add to visited, recursion_stack, and current_path
     * 2. Explore neighbors via execution flow connections
     * 3. For each neighbor:
     *    - If in recursion_stack: CYCLE DETECTED (back edge)
     *    - If not visited: recursively explore
     *    - If visited but not in stack: already fully explored, skip
     * 4. Exit node: remove from recursion_stack and current_path (backtrack)
     *
     * @param node the current node being explored
     * @param visited set of all visited nodes
     * @param recursion_stack set of nodes in current DFS path
     * @param current_path ordered path of nodes from root to current
     */
    private void dfs(
            IBlueprintNode node,
            Set<IBlueprintNode> visited,
            Set<IBlueprintNode> recursion_stack,
            List<IBlueprintNode> current_path
    ) {
        // Mark node as visited and add to current DFS path
        visited.add(node);
        recursion_stack.add(node);
        current_path.add(node);

        // Get all execution output pins from this node
        final List<? extends IBlueprintPin> exec_outputs = node.getOutputs().stream()
                .filter(IBlueprintPin::isExecution)
                .toList();

        // Explore all execution flow connections from this node
        for (final IBlueprintPin output_pin : exec_outputs) {
            for (final IBlueprintConnection connection : output_pin.getConnections()) {
                final IBlueprintPin input_pin = connection.getOther(output_pin);
                final IBlueprintNode next_node = input_pin.getNode();

                // CYCLE DETECTION: If next_node is in recursion_stack, it means:
                // 1. We're currently exploring next_node's subtree (it's an ancestor)
                // 2. We've found a "back edge" from descendant to ancestor
                // 3. This creates a cycle in the execution flow
                if (recursion_stack.contains(next_node)) {
                    // Reconstruct the cycle path for error reporting
                    // Extract the portion of current_path that forms the cycle
                    final List<IBlueprintNode> cycle_path = new ArrayList<>();
                    boolean found_start = false;

                    // Find where the cycle begins in the path
                    for (final IBlueprintNode path_node : current_path) {
                        if (path_node.equals(next_node)) {
                            found_start = true;
                        }
                        if (found_start) {
                            cycle_path.add(path_node);
                        }
                    }

                    // Complete the cycle by adding the back edge target
                    cycle_path.add(next_node);

                    throw new CycleDetectedException(cycle_path);
                }

                // EXPLORATION: If next_node hasn't been visited, explore its subtree
                // Note: If visited but not in recursion_stack, it's already fully explored
                if (!visited.contains(next_node)) {
                    this.dfs(next_node, visited, recursion_stack, current_path);
                }
            }
        }

        // BACKTRACKING: Remove node from recursion stack when leaving this subtree
        // The node remains in 'visited' but is no longer in the current path
        // This allows other paths to reach this node without false cycle detection
        recursion_stack.remove(node);
        current_path.removeLast();
    }
}
