package fr.boul2gom.blueprints.execution.context;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.execution.context.IIterationTracker;

import java.util.HashMap;
import java.util.Map;

/**
 * Provider implementation for tracking node iterations.
 * Uses 0-based indexing: first execution has index 0, second has index 1, etc.
 */
public class IterationTracker implements IIterationTracker {

    private final Map<IBlueprintNode, Integer> counts;

    public IterationTracker() {
        this.counts = new HashMap<>();
    }

    @Override
    public void increment(IBlueprintNode node) {
        final int current = this.getCount(node);
        this.counts.put(node, current + 1);
    }

    @Override
    public int getIndex(IBlueprintNode node) {
        // 0-based index: count - 1
        // If count is 0, no execution yet, return 0
        // If count is 1, first execution completed, currently on index 0
        final int count = this.getCount(node);
        return Math.max(0, count);
    }

    @Override
    public int getCount(IBlueprintNode node) {
        return this.counts.getOrDefault(node, 0);
    }

    @Override
    public void reset(IBlueprintNode node) {
        this.counts.remove(node);
    }

    @Override
    public boolean hasExceeded(IBlueprintNode node, int maxIterations) {
        return this.getCount(node) >= maxIterations;
    }

    @Override
    public void clear() {
        this.counts.clear();
    }

    @Override
    public String toString() {
        return String.format("IterationTracker(tracked_nodes=%d)", this.counts.size());
    }
}
