package fr.boul2gom.blueprints.api.exception.validation;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.List;

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

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cycle_path.size(); i++) {
            builder.append(cycle_path.get(i).getName());
            if (i < cycle_path.size() - 1) {
                builder.append(" -> ");
            }
        }

        return builder.toString();
    }
}
