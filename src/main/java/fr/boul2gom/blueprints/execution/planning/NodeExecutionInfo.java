package fr.boul2gom.blueprints.execution.planning;

import fr.boul2gom.blueprints.api.execution.NodeExecutionType;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

/**
 * Metadata about a node's execution characteristics in the DAG.
 * Computed during execution planning and used to optimize execution flow.
 */
public record NodeExecutionInfo(IBlueprintNode node, NodeExecutionType type, int exec_input_count, int exec_output_count) {
}
