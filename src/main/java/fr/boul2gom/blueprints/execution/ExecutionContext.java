package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.api.execution.IExecutionContext;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExecutionContext implements IExecutionContext {

    // Default execution timeout in milliseconds (50ms)
    public static final long DEFAULT_TIMEOUT = 50L;

    // Default maximum nodes per execution (10,000 nodes)
    public static final int DEFAULT_MAX_NODES = 10_000;

    private final Map<String, Object> variables;
    private final World world;
    private final Entity entity;
    private final long start_time;
    private final long timeout;
    private final int max_nodes;

    private IBlueprintNode current_node;
    private int nodes_executed;

    public ExecutionContext(@Nullable World world, @Nullable Entity entity) {
        this(world, entity, DEFAULT_TIMEOUT, DEFAULT_MAX_NODES);
    }

    public ExecutionContext(@Nullable World world, @Nullable Entity entity, long timeout, int max_nodes) {
        this.variables = new HashMap<>();
        this.world = world;
        this.entity = entity;
        this.start_time = System.currentTimeMillis();
        this.timeout = timeout;
        this.max_nodes = max_nodes;
        this.current_node = null;
        this.nodes_executed = 0;
    }

    @Override
    @Nullable
    public Object getVariable(String key) {
        Objects.requireNonNull(key, "Variable key may not be null");
        return this.variables.get(key);
    }

    @Override
    public void setVariable(String key, Object value) {
        Objects.requireNonNull(key, "Variable key may not be null");
        this.variables.put(key, value);
    }

    @Override
    public Map<String, Object> getVariables() {
        // Return unmodifiable view to prevent external modification
        return Map.copyOf(this.variables);
    }

    @Override
    @Nullable
    public World getWorld() {
        return this.world;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    @Nullable
    public IBlueprintNode getCurrentNode() {
        return this.current_node;
    }

    @Override
    public void setCurrentNode(IBlueprintNode node) {
        this.current_node = node;
    }

    @Override
    public long getStartTime() {
        return this.start_time;
    }

    @Override
    public int getNodesExecuted() {
        return this.nodes_executed;
    }

    @Override
    public void incrementNodesExecuted() {
        this.nodes_executed++;
    }

    @Override
    public boolean shouldStop() {
        // Check if execution time exceeded
        final long elapsed_time = System.currentTimeMillis() - this.start_time;
        if (elapsed_time > this.timeout) {
            return true;
        }

        // Check if node limit exceeded
        return this.nodes_executed >= this.max_nodes;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }
}
