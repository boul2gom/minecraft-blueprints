package fr.boul2gom.blueprints.graph;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.exception.NodeNotFoundException;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinDirection;
import fr.boul2gom.blueprints.api.pin.PinType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BlueprintGraph implements IBlueprintGraph {

    private final String id;
    private final String name;
    private final Set<IBlueprintNode> nodes;
    private final Set<IBlueprintConnection> connections;

    private final GraphValidator validator;
    private boolean is_valid;

    public BlueprintGraph(String id, String name) {
        Objects.requireNonNull(id, "Graph ID may not be null");
        Objects.requireNonNull(name, "Graph name may not be null");

        this.id = id;
        this.name = name;
        this.nodes = new HashSet<>();
        this.connections = new HashSet<>();
        this.validator = new GraphValidator(this);
        this.is_valid = true; // Empty graph is valid
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
    public void addNode(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");

        this.nodes.add(node);
        this.is_valid = false; // Mark as needing validation
    }

    @Override
    public void removeNode(IBlueprintNode node) {
        Objects.requireNonNull(node, "Node may not be null");

        if (!this.nodes.contains(node)) {
            throw new NodeNotFoundException(node.getId());
        }

        // Remove all connections involving this node
        final Set<IBlueprintConnection> connections_to_remove = this.connections.stream()
            .filter(connection -> connection.getInput().getNode().equals(node) ||
                                 connection.getOutput().getNode().equals(node))
            .collect(Collectors.toSet());

        connections_to_remove.forEach(this::removeConnection);

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
        return Collections.unmodifiableSet(this.nodes);
    }

    @Override
    public void addConnection(IBlueprintConnection connection) {
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
    public void removeConnection(IBlueprintConnection connection) {
        Objects.requireNonNull(connection, "Connection may not be null");

        // Disconnect the pins
        final IBlueprintPin input = connection.getInput();
        final IBlueprintPin output = connection.getOutput();
        input.disconnectFrom(output);

        this.connections.remove(connection);
        this.is_valid = false; // Mark as needing validation
    }

    @Override
    public Set<IBlueprintConnection> getConnections() {
        return Collections.unmodifiableSet(this.connections);
    }

    @Override
    public List<IBlueprintNode> getEntryPoints() {
        // Entry points are nodes with no incoming EXECUTION_FLOW connections
        return this.nodes.stream()
            .filter(node -> {
                // Get all execution input pins for this node
                final List<? extends IBlueprintPin> exec_inputs = node.getInputs().stream()
                    .filter(pin -> pin.getType() == PinType.EXECUTION_FLOW)
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
            input.disconnectFrom(output);
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
