package fr.boul2gom.blueprints.execution;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.execution.ExecutionException;
import fr.boul2gom.blueprints.api.exception.execution.ExecutionTimeoutException;
import fr.boul2gom.blueprints.api.exception.execution.NodeLimitExceededException;
import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.execution.ExecutionResult;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Async-aware blueprint executor with conditional flow and loop support.
 *
 * Executes blueprint graphs using recursive async traversal with the following features:
 * - Async execution via CompletableFuture
 * - Conditional flow (nodes control which output pins to follow)
 * - Loop support (nodes can be re-executed with iteration tracking)
 * - Performance monitoring and safety limits
 * - Execution logging and debugging
 */
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

        // Clear logger before execution
        context.getLogger().clear();

        try {
            // 1. Validate graph before execution
            graph.validate();

            // 2. Get entry points (nodes with no incoming execution flow)
            final List<IBlueprintNode> entry_points = graph.get_entry_points();

            if (entry_points.isEmpty()) {
                return ExecutionResult.validation("No entry points found in graph");
            }

            // 3. Execute using async BFS traversal
            // This returns a CompletableFuture that completes when execution finishes
            final CompletableFuture<Void> execution_future = this.execute_nodes(entry_points, context);

            // Block until execution completes (sync for now, async scheduling in Phase 3)
            execution_future.join();

            // 4. Log execution summary
            context.getLogger().summary();

            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.success(execution_time, context.get_nodes_executed());

        } catch (ValidationException e) {
            return ExecutionResult.validation(e.getMessage());

        } catch (ExecutionTimeoutException e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.timeout(execution_time, context.get_nodes_executed());

        } catch (NodeLimitExceededException e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.node_limit_exceeded(execution_time, context.get_nodes_executed());

        } catch (Exception e) {
            final Duration execution_time = Duration.between(start_time, Instant.now());
            return ExecutionResult.error(e.getMessage(), execution_time, context.get_nodes_executed());
        }
    }

    /**
     * Execute multiple nodes in parallel.
     * Each node execution returns a future that completes when the node and all its descendants complete.
     */
    private CompletableFuture<Void> execute_nodes(List<IBlueprintNode> nodes, IExecutionContext context) {
        if (nodes.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        // Execute all nodes in parallel
        final List<CompletableFuture<Void>> futures = nodes.stream()
                .map(node -> this.execute_node(node, context))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Execute a single node and recursively execute its descendants.
     * Supports loops by tracking iteration count per node.
     */
    private CompletableFuture<Void> execute_node(IBlueprintNode node, IExecutionContext context) {
        // Check iteration limit (prevents infinite loops)
        if (context.hasExceededIterations(node)) {
            throw new ExecutionException(
                String.format("Max iterations exceeded for node '%s' (limit: %d)",
                    node.getName(), IBlueprintExecutor.MAX_ITERATIONS_PER_LOOP)
            );
        }

        // Increment iteration counter
        context.increment_iterations(node);

        // Check performance limits before executing
        this.monitor.check(context);

        context.set_current_node(node);

        // Log node execution start
        final Instant node_start = context.getLogger().log_start(
                node,
                context.getVariables().snapshot()
        );

        return node.execute(context)
            .thenCompose(active_pins -> {
                context.increment_nodes();

                // Log node execution success
                context.getLogger().log_end(
                        node,
                        node_start,
                        context.getVariables().snapshot(),
                        true,
                        null
                );

                // Get next nodes based on active pins
                final List<IBlueprintNode> next_nodes = this.getNext(node, active_pins);

                // Execute next nodes recursively
                return this.execute_nodes(next_nodes, context);
            })
            .exceptionally(error -> {
                // Log node execution failure
                context.getLogger().log_end(
                        node,
                        node_start,
                        context.getVariables().snapshot(),
                        false,
                        error.getMessage()
                );

                throw new ExecutionException(
                    String.format("Error executing node '%s': %s", node.getName(), error.getMessage()),
                    error
                );
            });
    }

    /**
     * Get next nodes to execute based on active output pins.
     *
     * @param node the current node
     * @param active_pins set of active output pin IDs, or null/empty to follow all
     * @return list of next nodes to execute
     */
    private List<IBlueprintNode> getNext(IBlueprintNode node, Set<String> active_pins) {
        final List<IBlueprintNode> next_nodes = new ArrayList<>();

        // Get all execution output pins
        final List<? extends IBlueprintPin> exec_outputs = node.getOutputs().stream()
            .filter(IBlueprintPin::isExecution)
            .toList();

        // If no active pins specified (null or empty), follow all execution outputs
        final boolean follow_all = (active_pins == null || active_pins.isEmpty());

        // Follow each execution output connection
        for (final IBlueprintPin output_pin : exec_outputs) {
            // Check if this pin should be followed
            if (!follow_all && !active_pins.contains(output_pin.getId())) {
                continue; // Skip this pin
            }

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
