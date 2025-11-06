package fr.boul2gom.blueprints.execution.context;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.execution.context.IExecutionFrame;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of an execution frame with isolated pin value cache.
 * Each frame maintains its own cache to prevent cross-iteration pollution.
 *
 * The cache uses LRU (Least Recently Used) eviction policy with a max size of 1000 entries.
 * This prevents unbounded memory growth in long-running blueprints.
 */
public class ExecutionFrame implements IExecutionFrame {

    private static final int MAX_CACHE_SIZE = 1000;

    private final Map<IBlueprintPin, Object> pin_cache;
    private final int depth;
    private final IBlueprintNode owner;

    /**
     * Creates a root frame (depth 0, no owner).
     */
    public ExecutionFrame() {
        this(0, null);
    }

    /**
     * Creates a frame with specified depth and owner.
     *
     * @param depth The frame depth in the stack
     * @param owner The node that created this frame
     */
    public ExecutionFrame(int depth, IBlueprintNode owner) {
        // LinkedHashMap with access-order mode (true) for LRU behavior
        // When size exceeds MAX_CACHE_SIZE, eldest entry is automatically removed
        this.pin_cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<IBlueprintPin, Object> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
        this.depth = depth;
        this.owner = owner;
    }

    @Override
    public Optional<Object> get_pin_value(IBlueprintPin pin) {
        return Optional.ofNullable(this.pin_cache.get(pin));
    }

    @Override
    public void set_pin_value(IBlueprintPin pin, Object value) {
        this.pin_cache.put(pin, value);
    }

    @Override
    public boolean has_pin_value(IBlueprintPin pin) {
        return this.pin_cache.containsKey(pin);
    }

    @Override
    public void invalidate_node(IBlueprintNode node) {
        // Remove all pins belonging to this node
        node.getInputs().forEach(this.pin_cache::remove);
        node.getOutputs().forEach(this.pin_cache::remove);
    }

    @Override
    public void clear() {
        this.pin_cache.clear();
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public Optional<IBlueprintNode> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    @Override
    public IExecutionFrame create_child(IBlueprintNode owner) {
        return new ExecutionFrame(this.depth + 1, owner);
    }

    @Override
    public String toString() {
        return String.format("ExecutionFrame(depth=%d, cache_size=%d%s)",
            this.depth,
            this.pin_cache.size(),
            this.owner != null ? ", owner=" + this.owner.getId() : "");
    }
}
