package fr.boul2gom.blueprints.api.exception.validation;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception indicating that a cycle has been detected in a blueprint graph.
 * This exception is typically thrown during validation when a cyclic dependency
 * is found among nodes in the graph.
 *
 * The cycle itself can be inspected by retrieving the path of nodes involved using
 * the {@link #getCycle()} method.
 */
public class CycleDetectedException extends ValidationException {

    private final List<IBlueprintNode> cycle_path;

    public CycleDetectedException(List<IBlueprintNode> cycle_path) {
        super("Cycle detected in blueprint graph: " + format(cycle_path));
        this.cycle_path = cycle_path;
    }

    public List<IBlueprintNode> getCycle() {
        return this.cycle_path;
    }

    private static String format(List<IBlueprintNode> cycle_path) {
        if (cycle_path.isEmpty()) {
            return "[]";
        }

        // Use Java 8+ stream API with Collectors.joining for cleaner string concatenation
        return cycle_path.stream()
            .map(IBlueprintNode::getName)
            .collect(Collectors.joining(" -> "));
    }
}
