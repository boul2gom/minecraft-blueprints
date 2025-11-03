package fr.boul2gom.blueprints.api.exception;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.List;

// Exception thrown when a cycle is detected in the graph
public class CycleDetectedException extends ValidationException {

    private final List<IBlueprintNode> cycle_path;

    public CycleDetectedException(List<IBlueprintNode> cycle_path) {
        super("Cycle detected in blueprint graph: " + formatCyclePath(cycle_path));
        this.cycle_path = cycle_path;
    }

    public List<IBlueprintNode> getCyclePath() {
        return this.cycle_path;
    }

    private static String formatCyclePath(List<IBlueprintNode> cycle_path) {
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
