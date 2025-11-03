package fr.boul2gom.blueprints.graph;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.NodeNotFoundException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.graph.validation.GraphValidator;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlueprintGraph implements IBlueprintGraph {

    private final String id;
    private final String name;
    private final Set<IBlueprintNode> nodes;
    private final Set<IBlueprintConnection> connections;

    private final GraphValidator validator;
    private boolean is_valid;
    
    // Cached unmodifiable views
    private Set<IBlueprintNode> cachedUnmodifiableNodes;
    private Set<IBlueprintConnection> cachedUnmodifiableConnections;

    public BlueprintGraph(String id, String name) {
        Objects.requireNonNull(id, "Graph ID may not be null");
        Objects.requireNonNull(name, "Graph name may not be null");

        this.id = id;
        this.name = name;
        this.nodes = new HashSet<>();
        this.connections = new HashSet<>();
        this.validator = new GraphValidator(this);
        this.is_valid = true; // Empty graph is valid
        
        // Initialize cached views
        this.cachedUnmodifiableNodes = Collections.unmodifiableSet(this.nodes);
        this.cachedUnmodifiableConnections = Collections.unmodifiableSet(this.connections);
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
        // No need to update cache - unmodifiable view wraps the set
    }

    @Override
    public void remove(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");

        if (!this.nodes.contains(node)) {
            throw new NodeNotFoundException(node.getId());
        }

        // Remove all connections involving this node
        this.connections.stream()
            .filter(connection -> connection.getInput().getNode().equals(node) ||
                                 connection.getOutput().getNode().equals(node))
            .toList()  // Collect to list to avoid ConcurrentModificationException
            .forEach(this::remove_connection);

        // Remove the node
        this.nodes.remove(node);
        this.is_valid = false; // Mark as needing validation
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
        return this.cachedUnmodifiableNodes;
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
    }

    @Override
    public Set<IBlueprintConnection> getConnections() {
        return this.cachedUnmodifiableConnections;
    }

    @Override
    public List<IBlueprintNode> get_entry_points() {
        // Entry points are nodes with no incoming EXECUTION_FLOW connections
        return this.nodes.stream()
            .filter(this::isEntryPoint)
            .toList();
    }
    
    /**
     * Checks if a node is an entry point (has execution inputs but none are connected).
     */
    private boolean isEntryPoint(IBlueprintNode node) {
        // Get all execution input pins for this node
        final List<? extends IBlueprintPin> exec_inputs = node.getInputs().stream()
            .filter(IBlueprintPin::isExecution)
            .toList();

        // If node has no execution inputs, it's not an entry point
        if (exec_inputs.isEmpty()) {
            return false;
        }

        // Check if any execution input is connected
        return exec_inputs.stream().noneMatch(IBlueprintPin::isConnected);
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
        // Disconnect all pins
        for (final IBlueprintConnection connection : this.connections) {
            final IBlueprintPin input = connection.getInput();
            final IBlueprintPin output = connection.getOutput();
            input.disconnect_from(output);
        }

        this.connections.clear();
        this.nodes.clear();
        this.is_valid = true; // Empty graph is valid
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
