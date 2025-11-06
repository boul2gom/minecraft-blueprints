package fr.boul2gom.blueprints.graph;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.NodeNotFoundException;
import fr.boul2gom.blueprints.api.execution.planning.IExecutionPlan;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.graph.observer.TopologyEventType;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;
import fr.boul2gom.blueprints.api.pin.BlueprintPin;
import fr.boul2gom.blueprints.execution.planning.ExecutionPlanner;
import fr.boul2gom.blueprints.graph.observer.GraphTopologySubject;
import fr.boul2gom.blueprints.graph.observer.ValidationObserver;
import fr.boul2gom.blueprints.graph.validation.GraphValidator;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintGraph implements IBlueprintGraph {

    private final String id;
    private final String name;
    private final Set<IBlueprintNode> nodes;
    private final Set<IBlueprintConnection> connections;

    private final GraphValidator validator;
    private final GraphTopologySubject topology_subject;
    private boolean is_valid;

    // Performance optimization: cache entry points to avoid recomputation
    private List<IBlueprintNode> cached_entry_points;
    private boolean entry_points_dirty;
    private boolean batch_mode; // When true, defer cache invalidation until end_batch()

    // Performance optimization: cache execution plan to avoid recomputation
    private IExecutionPlan cached_execution_plan;
    private boolean execution_plan_dirty;

    public BlueprintGraph(String id, String name) {
        Objects.requireNonNull(id, "Graph ID may not be null");
        Objects.requireNonNull(name, "Graph name may not be null");

        this.id = id;
        this.name = name;
        this.nodes = new HashSet<>();
        this.connections = new HashSet<>();
        this.validator = new GraphValidator(this);
        this.is_valid = true; // Empty graph is valid

        // Initialize entry points cache
        this.cached_entry_points = null;
        this.entry_points_dirty = true;
        this.batch_mode = false;

        // Initialize execution plan cache
        this.cached_execution_plan = null;
        this.execution_plan_dirty = true;

        // Initialize observer pattern for automatic graph revalidation
        this.topology_subject = new GraphTopologySubject(this);
        this.topology_subject.register(new ValidationObserver());
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void add(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");

        this.nodes.add(node);
        this.is_valid = false; // Mark as needing validation

        // Defer cache invalidation if in batch mode
        if (!this.batch_mode) {
            this.entry_points_dirty = true; // Invalidate entry points cache
            this.execution_plan_dirty = true; // Invalidate execution plan cache
        }

        // Inject topology subject into all pins for observer notifications
        for (final IBlueprintPin pin : node.getInputs()) {
            if (pin instanceof BlueprintPin bp) {
                bp.set_topology_subject(this.topology_subject);
            }
        }
        for (final IBlueprintPin pin : node.getOutputs()) {
            if (pin instanceof BlueprintPin bp) {
                bp.set_topology_subject(this.topology_subject);
            }
        }
    }

    @Override
    public void remove(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");

        if (!this.nodes.contains(node)) {
            throw new NodeNotFoundException(node.getId());
        }

        // Remove all connections involving this node
        final Set<IBlueprintConnection> connections_to_remove = this.connections.stream()
            .filter(connection -> connection.getInput().getNode().equals(node) ||
                                 connection.getOutput().getNode().equals(node))
            .collect(Collectors.toSet());

        connections_to_remove.forEach(this::remove_connection);

        // Remove the node
        this.nodes.remove(node);
        this.is_valid = false; // Mark as needing validation

        // Defer cache invalidation if in batch mode
        if (!this.batch_mode) {
            this.entry_points_dirty = true; // Invalidate entry points cache
            this.execution_plan_dirty = true; // Invalidate execution plan cache
        }
    }

    @Override
    @Nullable
    public IBlueprintNode getNode(String id) {
        Objects.requireNonNull(id, "Node ID may not be null");

        return this.nodes.stream()
            .filter(node -> node.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<IBlueprintNode> getNodes() {
        return Collections.unmodifiableSet(this.nodes);
    }

    @Override
    public void add_connection(IBlueprintConnection connection) {
        Objects.requireNonNull(connection, "Connection may not be null");

        // Verify both nodes are in the graph
        final IBlueprintNode input_node = connection.getInput().getNode();
        final IBlueprintNode output_node = connection.getOutput().getNode();

        if (!this.nodes.contains(input_node)) {
            throw new NodeNotFoundException(input_node.getId());
        }

        if (!this.nodes.contains(output_node)) {
            throw new NodeNotFoundException(output_node.getId());
        }

        this.connections.add(connection);
        this.is_valid = false; // Mark as needing validation

        // Defer cache invalidation if in batch mode
        if (!this.batch_mode) {
            this.entry_points_dirty = true; // Invalidate entry points cache
            this.execution_plan_dirty = true; // Invalidate execution plan cache
        }
    }

    @Override
    public void remove_connection(IBlueprintConnection connection) {
        Objects.requireNonNull(connection, "Connection may not be null");

        // Disconnect the pins
        final IBlueprintPin input = connection.getInput();
        final IBlueprintPin output = connection.getOutput();
        input.disconnect_from(output);

        this.connections.remove(connection);
        this.is_valid = false; // Mark as needing validation

        // Defer cache invalidation if in batch mode
        if (!this.batch_mode) {
            this.entry_points_dirty = true; // Invalidate entry points cache
            this.execution_plan_dirty = true; // Invalidate execution plan cache
        }
    }

    @Override
    public Set<IBlueprintConnection> getConnections() {
        return Collections.unmodifiableSet(this.connections);
    }

    @Override
    public List<IBlueprintNode> get_entry_points() {
        // Use cached entry points if available
        if (!this.entry_points_dirty && this.cached_entry_points != null) {
            return this.cached_entry_points;
        }

        // Recompute entry points: nodes with no incoming EXECUTION_FLOW connections
        final List<IBlueprintNode> entry_points = this.nodes.stream()
            .filter(node -> {
                // Get all execution input pins for this node
                final List<? extends IBlueprintPin> exec_inputs = node.getInputs().stream()
                    .filter(IBlueprintPin::isExecution)
                    .toList();

                // If node has no execution inputs, it's not an entry point
                if (exec_inputs.isEmpty()) {
                    return false;
                }

                // Check if any execution input is connected
                for (final IBlueprintPin pin : exec_inputs) {
                    if (pin.isConnected()) {
                        return false; // Has incoming execution flow
                    }
                }

                return true; // Has execution input(s) but none are connected
            })
            .toList();

        // Cache the result
        this.cached_entry_points = entry_points;
        this.entry_points_dirty = false;

        return entry_points;
    }

    /**
     * Begins a batch operation on the graph.
     * Cache invalidation is deferred until end_batch() is called.
     * Useful when adding many nodes at once to avoid recomputing entry points repeatedly.
     *
     * Usage:
     * <pre>
     * graph.begin_batch();
     * try {
     *     graph.add(node1);
     *     graph.add(node2);
     *     // ... add many more nodes
     * } finally {
     *     graph.end_batch();
     * }
     * </pre>
     */
    public void begin_batch() {
        this.batch_mode = true;
    }

    /**
     * Ends a batch operation on the graph.
     * Invalidates the entry points cache if any modifications occurred during the batch.
     */
    public void end_batch() {
        this.batch_mode = false;
        // Invalidate cache now that batch is complete
        this.entry_points_dirty = true;
        this.execution_plan_dirty = true;
    }

    /**
     * Gets the cached execution plan for this graph.
     * If the plan is dirty (graph structure changed), it will be recomputed.
     *
     * @return the cached or freshly computed execution plan
     */
    public IExecutionPlan get_execution_plan() {
        // Use cached plan if available
        if (!this.execution_plan_dirty && this.cached_execution_plan != null) {
            return this.cached_execution_plan;
        }

        // Recompute execution plan
        final IExecutionPlan plan = ExecutionPlanner.create_plan(this);

        // Cache the result
        this.cached_execution_plan = plan;
        this.execution_plan_dirty = false;

        return plan;
    }

    @Override
    public void validate() {
        this.validator.validate();
        this.is_valid = true;
    }

    @Override
    public boolean isValid() {
        if (!this.is_valid) {
            try {
                this.validate();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        // Disconnect all pins (triggers CONNECTION_REMOVED notifications via pins)
        for (final IBlueprintConnection connection : this.connections) {
            final IBlueprintPin input = connection.getInput();
            final IBlueprintPin output = connection.getOutput();
            input.disconnect_from(output);
        }

        this.connections.clear();

        // Notify observers for each node removal before clearing
        for (final IBlueprintNode node : this.nodes) {
            this.topology_subject.notify_observers(
                TopologyEventType.NODE_REMOVED,
                "Clearing graph: " + node.getId()
            );
        }

        this.nodes.clear();
        this.is_valid = true; // Empty graph is valid

        // Invalidate caches (always, even in batch mode, since we're clearing everything)
        this.cached_entry_points = null;
        this.entry_points_dirty = true;
        this.cached_execution_plan = null;
        this.execution_plan_dirty = true;
    }

    @Override
    public String toString() {
        return String.format("BlueprintGraph(id=%s, name=%s, nodes=%d, connections=%d, valid=%b)",
            this.id, this.name, this.nodes.size(), this.connections.size(), this.is_valid);
    }
}
