package fr.boul2gom.blueprints.api.pin;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.connection.BlueprintConnection;
import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.graph.observer.GraphTopologySubject;
import fr.boul2gom.blueprints.api.graph.observer.TopologyEventType;

import java.lang.ref.WeakReference;
import java.util.*;

public class BlueprintPin implements IBlueprintPin {

    private final String id;
    private final String name;
    private final PinType type;
    private final PinDirection direction;
    private final IBlueprintNode node;

    // All connections this pin is part of (INPUT: max 1, OUTPUT: many)
    private final Set<IBlueprintConnection> connections;

    // Observer subject for notifying topology changes (injected by BlueprintGraph)
    // WeakReference prevents memory leaks if pins are not properly removed from graphs
    private WeakReference<GraphTopologySubject> topology_subject;

    public BlueprintPin(String id, String name, PinType type, PinDirection direction, IBlueprintNode node) {
        Objects.requireNonNull(id, "Pin id may not be null");
        Objects.requireNonNull(name, "Pin name may not be null");
        Objects.requireNonNull(type, "Pin type may not be null");
        Objects.requireNonNull(direction, "Pin direction may not be null");
        Objects.requireNonNull(node, "Pin owner node may not be null");

        this.id = id;
        this.name = name;
        this.type = type;
        this.direction = direction;
        this.node = node;
        this.connections = new HashSet<>();
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
    public PinType getType() {
        return this.type;
    }

    @Override
    public boolean isExecution() {
        return this.type.isExecution();
    }

    @Override
    public PinDirection getDirection() {
        return this.direction;
    }

    @Override
    public IBlueprintNode getNode() {
        return this.node;
    }

    /**
     * Set the topology subject for notifying observers of connection changes.
     * This is called by BlueprintGraph when pins are added to nodes.
     * Uses WeakReference to prevent memory leaks from circular references.
     *
     * @param subject the topology subject
     */
    public void set_topology_subject(GraphTopologySubject subject) {
        this.topology_subject = subject != null ? new WeakReference<>(subject) : null;
    }

    @Override
    public boolean isConnected() {
        return !this.connections.isEmpty();
    }

    @Override
    public boolean is_connected_to(IBlueprintPin pin) {
        Objects.requireNonNull(pin, "Pin may not be null");

        return this.connections.stream()
            .anyMatch(conn -> conn.getInput().equals(pin) || conn.getOutput().equals(pin));
    }

    @Override
    public boolean can_connect_to(IBlueprintPin pin) {
        Objects.requireNonNull(pin, "Pin may not be null");

        // Cannot connect to itself
        if (this.equals(pin)) {
            return false;
        }

        // Cannot connect pins from the same node
        if (this.node.equals(pin.getNode())) {
            return false;
        }

        // Pins must have opposite directions (INPUT <-> OUTPUT)
        if (this.direction == pin.getDirection()) {
            return false;
        }

        final PinType other_type = pin.getType();
        if (!this.type.is_compatible_with(other_type)) {
            return false;
        }

        // INPUT pins can only have ONE connection
        if (this.direction == PinDirection.INPUT && this.isConnected()) {
            return false;
        }

        // Check if the other pin (if INPUT) already has a connection
        return pin.getDirection() != PinDirection.INPUT || !pin.isConnected();
    }

    @Override
    public void connect_to(IBlueprintPin pin) {
        Objects.requireNonNull(pin, "Pin may not be null");

        // Validate connection before creating it
        if (!this.can_connect_to(pin)) {
            throw new IllegalArgumentException(
                "Cannot connect '" + this.getDisplay() + "' to '" + pin.getDisplay() + "' " +
                "(incompatible types, directions, or connection limit reached)"
            );
        }

        // Determine which pin is OUTPUT and which is INPUT
        final IBlueprintPin output = this.direction == PinDirection.OUTPUT ? this : pin;
        final IBlueprintPin input = this.direction == PinDirection.INPUT ? this : pin;

        // Create the connection with a unique ID
        final String connection_id = UUID.randomUUID().toString();
        final IBlueprintConnection connection = new BlueprintConnection(connection_id, input, output);

        // Add a connection to both pins
        this.connections.add(connection);
        if (pin instanceof BlueprintPin other) {
            other.connections.add(connection);
        }

        // Notify observers of topology change
        final GraphTopologySubject subject = this.topology_subject != null ? this.topology_subject.get() : null;
        if (subject != null) {
            final String context = String.format("%s -> %s", output.getId(), input.getId());
            subject.notify_observers(TopologyEventType.CONNECTION_ADDED, context);
        }
    }

    @Override
    public void disconnect() {
        // Copy to avoid a concurrent modification exception
        final Set<IBlueprintConnection> to_remove = new HashSet<>(this.connections);

        for (final IBlueprintConnection connection : to_remove) {
            // Find the other pin in this connection
            final IBlueprintPin other_pin = connection.getOther(this);

            // Remove connection from the other pin
            if (other_pin instanceof BlueprintPin other) {
                other.connections.remove(connection);
            }
        }

        // Clear all connections from this pin
        this.connections.clear();

        // Notify observers of topology change
        final GraphTopologySubject subject = this.topology_subject != null ? this.topology_subject.get() : null;
        if (subject != null) {
            final String context = String.format("All connections from %s", this.getId());
            subject.notify_observers(TopologyEventType.CONNECTION_REMOVED, context);
        }
    }

    @Override
    public void disconnect_from(IBlueprintPin pin) {
        Objects.requireNonNull(pin, "Pin may not be null");

        // Find the connection between this pin and the target pin
        final Optional<IBlueprintConnection> optional = this.connections.stream()
            .filter(conn -> conn.getInput().equals(pin) || conn.getOutput().equals(pin))
            .findFirst();

        if (optional.isEmpty()) {
            return; // No connection exists
        }

        // Remove connection from both pins
        final IBlueprintConnection connection = optional.get();
        this.connections.remove(connection);

        if (pin instanceof BlueprintPin other) {
            other.connections.remove(connection);
        }

        // Notify observers of topology change
        final GraphTopologySubject subject = this.topology_subject != null ? this.topology_subject.get() : null;
        if (subject != null) {
            final String context = String.format("%s disconnected from %s", this.getId(), pin.getId());
            subject.notify_observers(TopologyEventType.CONNECTION_REMOVED, context);
        }
    }

    @Override
    public Set<IBlueprintConnection> getConnections() {
        return Collections.unmodifiableSet(this.connections);
    }

    @Override
    public Set<IBlueprintPin> getPins() {
        final Set<IBlueprintPin> pins = new HashSet<>();

        for (final IBlueprintConnection connection : this.connections) {
            // Get the other pin from this connection
            final IBlueprintPin other_pin = connection.getOther(this);
            pins.add(other_pin);
        }

        return Collections.unmodifiableSet(pins);
    }

    @Override
    public String getDisplay() {
        return this.node.getName() + "." + this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BlueprintPin other)) {
            return false;
        }

        // Pins are equal if they have the same ID and belong to the same node
        return Objects.equals(this.id, other.id) && Objects.equals(this.node, other.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.node);
    }

    @Override
    public String toString() {
        return String.format("BlueprintPin(id=%s, name=%s, type=%s, direction=%s, node=%s, connected=%b)",
            this.id, this.name, this.type, this.direction, this.node.getId(), this.isConnected());
    }
}
