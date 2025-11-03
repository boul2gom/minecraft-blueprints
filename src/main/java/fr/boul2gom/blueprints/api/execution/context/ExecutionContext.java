package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Default implementation of IExecutionContext.
 * Provides a complete execution environment for blueprint execution including
 * variable storage, world/entity context, and safety limit enforcement.
 */
public class ExecutionContext implements IExecutionContext {

    private final IVariableRegistry variables;
    private final World world;
    private final Entity entity;
    private final Instant start_time;
    private final Duration timeout;
    private final int max_nodes;

    private IBlueprintNode current_node;
    private int nodes_executed;

    public ExecutionContext(@Nullable final World world, @Nullable final Entity entity) {
        this(world, entity, IBlueprintExecutor.MAX_EXECUTION_TIME, IBlueprintExecutor.MAX_NODES_PER_EXECUTION);
    }

    public ExecutionContext(@Nullable final World world, @Nullable final Entity entity, Duration timeout, final int max_nodes) {
        this.variables = new VariableRegistry();
        this.start_time = Instant.now();
        this.world = world;
        this.entity = entity;
        this.timeout = timeout;
        this.max_nodes = max_nodes;

        this.current_node = null;
        this.nodes_executed = 0;
    }

    @Override
    public IVariableRegistry getVariables() {
        return this.variables;
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
    public void setCurrentNode(final IBlueprintNode node) {
        this.current_node = node;
    }

    @Override
    public Instant getStartTime() {
        return this.start_time;
    }

    @Override
    public int getNodesExecuted() {
        return this.nodes_executed;
    }

    @Override
    public void incrementNodes() {
        this.nodes_executed++;
    }

    @Override
    public Duration getTimeout() {
        return this.timeout;
    }

    @Override
    public int getMaxNodes() {
        return this.max_nodes;
    }

    @Override
    public boolean isStoppingNeeded() {
        // Check if execution time exceeded
        final Duration elapsed_time = Duration.between(this.start_time, Instant.now());
        if (elapsed_time.compareTo(this.timeout) > 0) {
            return true;
        }

        // Check if node limit exceeded
        return this.nodes_executed >= this.max_nodes;
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
