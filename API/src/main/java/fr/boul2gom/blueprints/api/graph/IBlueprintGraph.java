package fr.boul2gom.blueprints.api.graph;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Represents a blueprint graph in a system, consisting of nodes and connections.
 * Provides methods for the management of nodes, their interconnections, and graph validation.
 */
public interface IBlueprintGraph {

    /**
     * Get the unique identifier of the graph.
     * @return unique identifier of the graph.
     */
    String getId();

    /**
     * Get the name of the graph.
     * @return name of the graph.
     */
    String getName();

    /**
     * Add a node to the graph.
     * @param node the node to add.
     */
    void add(IBlueprintNode node);

    /**
     * Remove a node from the graph.
     * @param node the node to remove.
     */
    void remove(IBlueprintNode node);

    /**
     * Get a node from the graph by its unique identifier.
     * @param id the unique identifier of the node.
     * @return the node, or null if not found.
     */
    @Nullable
    IBlueprintNode getNode(String id);

    /**
     * Get all nodes in the graph.
     * @return a set of nodes.
     */
    Set<IBlueprintNode> getNodes();

    /**
     * Add a connection to the graph.
     * @param connection the connection to add.
     */
    void add_connection(IBlueprintConnection connection);

    /**
     * Remove a connection from the graph.
     * @param connection the connection to remove.
     */
    void remove_connection(IBlueprintConnection connection);

    /**
     * Get all connections in the graph.
     * @return a set of connections.
     */
    Set<IBlueprintConnection> getConnections();

    /**
     * Get the entry points of the graph (nodes with no incoming execution flow connections).
     * @return a list of entry points.
     */
    List<IBlueprintNode> get_entry_points();

    /**
     * Validate the graph (check for cycles, disconnected pins, etc.)
     */
    void validate();

    /**
     * Check if the graph is valid, without throwing an exception.
     * @return true if the graph is valid, false otherwise.
     */
    boolean isValid();

    /**
     * Clear the graph, removing all nodes and connections.
     */
    void clear();
}
