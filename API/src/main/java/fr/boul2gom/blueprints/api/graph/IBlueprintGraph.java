package fr.boul2gom.blueprints.api.graph;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface IBlueprintGraph {

    // Get unique graph identifier
    String getId();

    // Get graph display name
    String getName();

    // Add a node to the graph
    void addNode(IBlueprintNode node);

    // Remove a node from the graph
    void removeNode(IBlueprintNode node);

    // Get a node by its ID
    @Nullable
    IBlueprintNode getNode(String id);

    // Get all nodes in the graph
    Set<IBlueprintNode> getNodes();

    // Add a connection between two pins
    void addConnection(IBlueprintConnection connection);

    // Remove a connection
    void removeConnection(IBlueprintConnection connection);

    // Get all connections in the graph
    Set<IBlueprintConnection> getConnections();

    // Get entry points (nodes with no incoming execution flow connections)
    List<IBlueprintNode> getEntryPoints();

    // Validate the graph (check for cycles, disconnected pins, etc.)
    void validate();

    // Check if graph is valid without throwing exception
    boolean isValid();

    // Clear all nodes and connections
    void clear();
}
