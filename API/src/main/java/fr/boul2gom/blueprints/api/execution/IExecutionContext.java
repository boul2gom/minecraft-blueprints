package fr.boul2gom.blueprints.api.execution;

import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IExecutionContext {

    // Get runtime variable value by key
    @Nullable
    Object getVariable(String key);

    // Set runtime variable value
    void setVariable(String key, Object value);

    // Get all runtime variables
    Map<String, Object> getVariables();

    // Get the world context for this execution
    @Nullable
    World getWorld();

    // Get the entity context for this execution (optional)
    @Nullable
    Entity getEntity();

    // Get the current node being executed
    @Nullable
    IBlueprintNode getCurrentNode();

    // Set the current node being executed (used by executor)
    void setCurrentNode(IBlueprintNode node);

    // Get execution start time in milliseconds
    long getStartTime();

    // Get number of nodes executed so far
    int getNodesExecuted();

    // Increment nodes executed counter (used by executor)
    void incrementNodesExecuted();

    // Check if execution should stop (timeout or node limit exceeded)
    boolean shouldStop();

    // Get execution timeout in milliseconds (default: 50ms)
    long getTimeout();
}
