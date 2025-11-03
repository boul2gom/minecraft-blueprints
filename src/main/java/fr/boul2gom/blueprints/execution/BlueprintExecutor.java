package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.ExecutionException;
import fr.boul2gom.blueprints.api.exception.ExecutionTimeoutException;
import fr.boul2gom.blueprints.api.exception.NodeLimitExceededException;
import fr.boul2gom.blueprints.api.exception.ValidationException;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.IExecutionContext;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.*;

public class BlueprintExecutor implements IBlueprintExecutor {

    private final PerformanceMonitor performance_monitor;

    public BlueprintExecutor() {
        this(ExecutionContext.DEFAULT_TIMEOUT, ExecutionContext.DEFAULT_MAX_NODES);
    }

    public BlueprintExecutor(long max_execution_time, int max_nodes_per_execution) {
        this.performance_monitor = new PerformanceMonitor(max_execution_time, max_nodes_per_execution);
    }

    @Override
    public IExecutionResult execute(IBlueprintGraph graph, IExecutionContext context) {
        Objects.requireNonNull(graph, "Graph may not be null");
        Objects.requireNonNull(context, "Execution context may not be null");

        final long start_time = System.currentTimeMillis();

        try {
            // 1. Validate graph before execution
            graph.validate();

            // 2. Get entry points (nodes with no incoming execution flow)
            final List<IBlueprintNode> entry_points = graph.getEntryPoints();

            if (entry_points.isEmpty()) {
                return ExecutionResult.validationFailed("No entry points found in graph");
            }

            // 3. Execute using BFS traversal
            this.executeBFS(entry_points, context);

            // 4. Return success result
            final long execution_time = System.currentTimeMillis() - start_time;
            return ExecutionResult.success(execution_time, context.getNodesExecuted());

        } catch (ValidationException e) {
            // Graph validation failed
            return ExecutionResult.validationFailed(e.getMessage());

        } catch (ExecutionTimeoutException e) {
            // Execution timeout
            final long execution_time = System.currentTimeMillis() - start_time;
            return ExecutionResult.timeout(execution_time, context.getNodesExecuted());

        } catch (NodeLimitExceededException e) {
            // Node limit exceeded
            final long execution_time = System.currentTimeMillis() - start_time;
            return ExecutionResult.nodeLimitExceeded(execution_time, context.getNodesExecuted());

        } catch (Exception e) {
            // Other execution error
            final long execution_time = System.currentTimeMillis() - start_time;
            return ExecutionResult.error(e.getMessage(), execution_time, context.getNodesExecuted());
        }
    }

    // Execute nodes using Breadth-First Search (BFS) with a queue
    private void executeBFS(List<IBlueprintNode> entry_points, IExecutionContext context) {
        // Queue for BFS traversal
        final Queue<IBlueprintNode> execution_queue = new LinkedList<>();
        final Set<IBlueprintNode> visited = new HashSet<>();

        // Add all entry points to the queue
        execution_queue.addAll(entry_points);

        // Process nodes in BFS order
        while (!execution_queue.isEmpty()) {
            // Check performance limits before processing next node
            this.performance_monitor.checkLimits(context);

            // Get next node from queue
            final IBlueprintNode current_node = execution_queue.poll();

            // Skip if already visited (can happen with multiple paths to same node)
            if (visited.contains(current_node)) {
                continue;
            }

            // Mark as visited
            visited.add(current_node);

            // Set current node in context
            context.setCurrentNode(current_node);

            try {
                // Execute the node
                current_node.execute(context);

                // Increment nodes executed counter
                context.incrementNodesExecuted();

                // Find next nodes to execute by following execution output pins
                final List<IBlueprintNode> next_nodes = this.getNextNodes(current_node);

                // Add next nodes to queue
                execution_queue.addAll(next_nodes);

            } catch (Exception e) {
                throw new ExecutionException(
                    String.format("Error executing node '%s': %s", current_node.getName(), e.getMessage()),
                    e
                );
            }
        }
    }

    // Get next nodes to execute by following execution output connections
    private List<IBlueprintNode> getNextNodes(IBlueprintNode node) {
        final List<IBlueprintNode> next_nodes = new ArrayList<>();

        // Get all execution output pins
        final List<? extends IBlueprintPin> exec_outputs = node.getOutputs().stream()
            .filter(pin -> pin.getType() == PinType.EXECUTION_FLOW)
            .toList();

        // Follow each execution output connection
        for (final IBlueprintPin output_pin : exec_outputs) {
            for (final IBlueprintConnection connection : output_pin.getConnections()) {
                // Get the connected input pin and its node
                final IBlueprintPin input_pin = connection.getOther(output_pin);
                final IBlueprintNode next_node = input_pin.getNode();

                next_nodes.add(next_node);
            }
        }

        return next_nodes;
    }

    @Override
    public long getMaxExecutionTime() {
        return this.performance_monitor.getMaxExecutionTime();
    }

    @Override
    public int getMaxNodesPerExecution() {
        return this.performance_monitor.getMaxNodesPerExecution();
    }
}
