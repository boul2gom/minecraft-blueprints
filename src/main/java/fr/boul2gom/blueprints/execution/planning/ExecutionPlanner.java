package fr.boul2gom.blueprints.execution.planning;

import fr.boul2gom.blueprints.api.execution.planning.IExecutionPlan;
import fr.boul2gom.blueprints.api.execution.NodeExecutionType;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.*;

/**
 * Analyzes blueprint graph topology to create execution plans.
 * Determines which nodes can execute in parallel and which require synchronization.
 *
 * Algorithm (DAG system):
 * 1. For each node, count execution input/output connections
 * 2. Classify nodes:
 *    - ENTRY: 0 exec inputs (event nodes)
 *    - SEQUENTIAL: 1 exec output
 *    - FORK: 2+ exec outputs (creates parallel branches)
 *    - JOIN: 2+ exec inputs (synchronization point)
 * 3. Build execution plan with join point tracking
 */
public class ExecutionPlanner {

    private static final int MIN_JOIN_INPUTS = 2;
    private static final int MIN_FORK_OUTPUTS = 2;

    /**
     * Create an execution plan for a graph by analyzing its topology.
     *
     * @param graph the blueprint graph to analyze
     * @return execution plan with node classification and join point tracking
     */
    public static IExecutionPlan create_plan(IBlueprintGraph graph) {
        final Map<IBlueprintNode, NodeExecutionInfo> node_info = new HashMap<>();
        final Set<IBlueprintNode> entry_points = new HashSet<>();

        // Analyze each node in the graph
        for (final IBlueprintNode node : graph.getNodes()) {
            final int exec_inputs = count_exec_inputs(node);
            final int exec_outputs = count_exec_outputs(node);

            // Classify node based on execution flow topology
            final NodeExecutionType type = classify_node(exec_inputs, exec_outputs);

            // Store node execution info
            final NodeExecutionInfo info = new NodeExecutionInfo(node, type, exec_inputs, exec_outputs);
            node_info.put(node, info);

            // Track entry points
            if (type == NodeExecutionType.ENTRY) {
                entry_points.add(node);
            }
        }

        return new ExecutionPlan(node_info, entry_points);
    }

    /**
     * Count the number of execution input pins with connections.
     *
     * @param node the node to analyze
     * @return number of connected execution inputs
     */
    private static int count_exec_inputs(IBlueprintNode node) {
        int count = 0;

        for (final IBlueprintPin input : node.getInputs()) {
            if (input.isExecution() && input.isConnected()) {
                // Count number of connections to this exec input
                count += input.getConnections().size();
            }
        }

        return count;
    }

    /**
     * Count the number of execution output connections.
     * A fork occurs when a node has multiple exec output connections,
     * allowing one node's execution to trigger multiple downstream nodes.
     *
     * This is consistent with count_exec_inputs() which also counts connections.
     *
     * @param node the node to analyze
     * @return number of connected execution outputs
     */
    private static int count_exec_outputs(IBlueprintNode node) {
        int count = 0;

        for (final IBlueprintPin output : node.getOutputs()) {
            if (output.isExecution() && output.isConnected()) {
                // Count number of connections from this exec output
                count += output.getConnections().size();
            }
        }

        return count;
    }

    /**
     * Classify a node based on its execution input/output counts.
     *
     * Rules:
     * - 0 exec inputs: ENTRY (event node)
     * - 2+ exec inputs: JOIN (synchronization point)
     * - 2+ exec outputs: FORK (parallel branching)
     * - 1 exec output: SEQUENTIAL (linear flow)
     *
     * Priority: JOIN > FORK > ENTRY > SEQUENTIAL
     *
     * @param exec_inputs number of execution inputs
     * @param exec_outputs number of execution outputs
     * @return node execution type
     */
    private static NodeExecutionType classify_node(int exec_inputs, int exec_outputs) {
        // JOIN has highest priority (synchronization required)
        if (exec_inputs >= MIN_JOIN_INPUTS) {
            return NodeExecutionType.JOIN;
        }

        // FORK for parallel execution
        if (exec_outputs >= MIN_FORK_OUTPUTS) {
            return NodeExecutionType.FORK;
        }

        // ENTRY for event nodes
        if (exec_inputs == 0) {
            return NodeExecutionType.ENTRY;
        }

        // Default: SEQUENTIAL
        return NodeExecutionType.SEQUENTIAL;
    }
}
