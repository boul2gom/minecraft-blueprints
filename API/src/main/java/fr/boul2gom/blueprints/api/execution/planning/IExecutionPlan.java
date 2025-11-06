package fr.boul2gom.blueprints.api.execution.planning;

import fr.boul2gom.blueprints.api.execution.NodeExecutionType;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.Set;

/**
 * Execution plan for a blueprint graph, computed by analyzing the DAG topology.
 * Provides information about:
 * - Node execution types (SEQUENTIAL, FORK, JOIN, ENTRY)
 * - Parallel vs sequential execution opportunities
 * - Join point synchronization requirements
 *
 * The plan is computed once before execution and cached for performance.
 */
public interface IExecutionPlan {

    /**
     * Get the execution type for a node.
     *
     * @param node the node to classify
     * @return the execution type (SEQUENTIAL, FORK, JOIN, ENTRY)
     */
    NodeExecutionType get_execution_type(IBlueprintNode node);

    /**
     * Check if a node is a join point requiring synchronization.
     * Join nodes must wait for all incoming execution branches to complete.
     *
     * @param node the node to check
     * @return true if the node is a join point
     */
    boolean is_join_point(IBlueprintNode node);

    /**
     * Get the number of incoming execution paths for a join node.
     * Used to determine how many branches must complete before executing the node.
     *
     * @param node the join node
     * @return number of incoming execution paths
     */
    int get_join_count(IBlueprintNode node);

    /**
     * Get all nodes in the execution plan.
     *
     * @return set of all nodes in the plan
     */
    Set<IBlueprintNode> getNodes();

    /**
     * Get the entry points of the graph (nodes with no execution inputs).
     *
     * @return set of entry point nodes
     */
    Set<IBlueprintNode> get_entry_points();
}
