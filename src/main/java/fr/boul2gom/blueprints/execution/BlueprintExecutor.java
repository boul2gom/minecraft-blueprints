package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.execution.ExecutionException;
import fr.boul2gom.blueprints.api.exception.execution.ExecutionTimeoutException;
import fr.boul2gom.blueprints.api.exception.execution.NodeLimitExceededException;
import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.execution.ExecutionResult;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.context.ExecutionContext;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class BlueprintExecutor implements IBlueprintExecutor {

    private final PerformanceMonitor monitor;

    public BlueprintExecutor() {
        this(IBlueprintExecutor.MAX_EXECUTION_TIME, IBlueprintExecutor.MAX_NODES_PER_EXECUTION);
    }

    public BlueprintExecutor(Duration max_execution_time, int max_nodes_per_execution) {
        this.monitor = new PerformanceMonitor(max_execution_time, max_nodes_per_execution);
    }

    @Override
    public IExecutionResult execute(IBlueprintGraph graph, IExecutionContext context) {
        Objects.requireNonNull(graph, "Graph may not be null");
        Objects.requireNonNull(context, "Execution context may not be null");

        final Instant start_time = Instant.now();

        try {
            // 1. Validate graph before execution
            graph.validate();

            // 2. Get entry points (nodes with no incoming execution flow)
            final List<IBlueprintNode> entry_points = graph.getEntryPoints();

            if (entry_points.isEmpty()) {
                return ExecutionResult.validation("No entry points found in graph");
            }

            // 3. Execute using BFS traversal
            this.bfs(entry_points, context);

            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.success(execution_time, context.getNodesExecuted());

        } catch (ValidationException e) {
            return ExecutionResult.validation(e.getMessage());

        } catch (ExecutionTimeoutException e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.timeout(execution_time, context.getNodesExecuted());

        } catch (NodeLimitExceededException e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.nodeLimitExceeded(execution_time, context.getNodesExecuted());

        } catch (Exception e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.error(e.getMessage(), execution_time, context.getNodesExecuted());
        }
    }

    // Execute nodes using Breadth-First Search (BFS) with a queue
    private void bfs(List<IBlueprintNode> entry_points, IExecutionContext context) {
        final Set<IBlueprintNode> visited = new HashSet<>();
        final Queue<IBlueprintNode> execution_queue = new LinkedList<>(entry_points);

        // Process nodes in BFS order
        while (!execution_queue.isEmpty()) {
            // Check performance limits before processing next node
            this.monitor.check(context);

            final IBlueprintNode current_node = execution_queue.poll();
            if (visited.contains(current_node)) {
                continue;
            }
            if (current_node == null) {
                continue;
            }

            visited.add(current_node);
            context.setCurrentNode(current_node);

            try {
                current_node.execute(context);
                context.incrementNodes();

                // Find next nodes to execute by following execution output pins
                final List<IBlueprintNode> next_nodes = this.getNext(current_node);
                execution_queue.addAll(next_nodes);

            } catch (Exception e) {
                throw new ExecutionException(
                    String.format("Error executing node '%s': %s", current_node.getName(), e.getMessage()),
                    e
                );
            }
        }
    }

    private List<IBlueprintNode> getNext(IBlueprintNode node) {
        final List<IBlueprintNode> next_nodes = new ArrayList<>();

        final List<? extends IBlueprintPin> exec_outputs = node.getOutputs().stream()
            .filter(IBlueprintPin::isExecution)
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
}
