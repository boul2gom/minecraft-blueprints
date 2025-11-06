package fr.boul2gom.blueprints.nodes;

import fr.boul2gom.blueprints.api.exception.NodeNotFoundException;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for node factories. Allows registration and instantiation of nodes by ID.
 *
 * Thread-safety: This registry is thread-safe for concurrent access.
 * - Uses ConcurrentHashMap for lock-free reads
 * - register() and create() can be called from multiple threads
 */
public class NodeRegistry {

    private static final Map<String, NodeFactory> FACTORIES = new ConcurrentHashMap<>();

    /**
     * Registers a node factory with a unique ID.
     * @param id the unique identifier for this node type
     * @param factory the factory to create instances of this node
     */
    public static void register(String id, NodeFactory factory) {
        if (FACTORIES.containsKey(id)) {
            throw new IllegalStateException("Node factory already registered for ID: " + id);
        }
        FACTORIES.put(id, factory);
    }

    /**
     * Creates a new node instance by ID.
     * @param id the node type ID
     * @param position the position of the node in the blueprint canvas
     * @return a new node instance
     * @throws NodeNotFoundException if no factory is registered for this ID
     */
    public static IBlueprintNode create(String id, NodePosition position) {
        final NodeFactory factory = FACTORIES.get(id);
        if (factory == null) {
            throw new NodeNotFoundException(id);
        }
        return factory.create(position);
    }

    /**
     * Gets all registered node IDs.
     * Returns an immutable copy to prevent external modification.
     *
     * @return an immutable set of all registered node IDs
     */
    public static Set<String> get_all_node_ids() {
        return Set.copyOf(FACTORIES.keySet());
    }

    /**
     * Checks if a node ID is registered.
     * @param id the node ID to check
     * @return true if the node is registered, false otherwise
     */
    public static boolean isRegistered(String id) {
        return FACTORIES.containsKey(id);
    }

    /**
     * Clears all registered factories.
     * This is useful for testing and lifecycle management (e.g., mod reload, server shutdown).
     * After calling this method, all previously registered nodes will be unregistered.
     */
    public static void clear() {
        FACTORIES.clear();
    }
}
