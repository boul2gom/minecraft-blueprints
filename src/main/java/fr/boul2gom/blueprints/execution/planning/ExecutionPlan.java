package fr.boul2gom.blueprints.execution.planning;

import fr.boul2gom.blueprints.api.execution.planning.IExecutionPlan;
import fr.boul2gom.blueprints.api.execution.NodeExecutionType;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Concrete execution plan storing node execution metadata.
 * Built by DAGExecutionPlanner and cached for performance.
 */
public class ExecutionPlan implements IExecutionPlan {

    private final Map<IBlueprintNode, NodeExecutionInfo> node_info;
    private final Set<IBlueprintNode> entry_points;

    public ExecutionPlan() {
        this.node_info = new HashMap<>();
        this.entry_points = Set.of();
    }

    public ExecutionPlan(
            Map<IBlueprintNode, NodeExecutionInfo> node_info,
            Set<IBlueprintNode> entry_points
    ) {
        this.node_info = node_info;
        this.entry_points = entry_points;
    }

    @Override
    public NodeExecutionType get_execution_type(IBlueprintNode node) {
        final NodeExecutionInfo info = this.node_info.get(node);
        return info != null ? info.type() : NodeExecutionType.SEQUENTIAL;
    }

    @Override
    public boolean is_join_point(IBlueprintNode node) {
        final NodeExecutionInfo info = this.node_info.get(node);

        return info != null && info.type() == NodeExecutionType.JOIN;
    }

    @Override
    public int get_join_count(IBlueprintNode node) {
        final NodeExecutionInfo info = this.node_info.get(node);

        return info != null ? info.exec_input_count() : 0;
    }

    @Override
    public Set<IBlueprintNode> getNodes() {
        return this.node_info.keySet();
    }

    @Override
    public Set<IBlueprintNode> get_entry_points() {
        return this.entry_points;
    }

    /**
     * Get detailed execution info for a node.
     *
     * @param node the node
     * @return execution info, or null if not in plan
     */
    public NodeExecutionInfo get_info(IBlueprintNode node) {
        return this.node_info.get(node);
    }

    @Override
    public String toString() {
        return String.format("ExecutionPlan(nodes=%d, entry_points=%d)",
            this.node_info.size(),
            this.entry_points.size());
    }
}
