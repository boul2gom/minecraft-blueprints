package fr.boul2gom.blueprints.api.graph.observer;

/**
 * Types of topology change events that can occur in a blueprint graph.
 * These events trigger graph revalidation to maintain structural integrity.
 */
public enum TopologyEventType {
    /**
     * A connection was added between two pins.
     * This may introduce new dependencies or cycles.
     */
    CONNECTION_ADDED,

    /**
     * A connection was removed between two pins.
     * This may break execution paths or data flow.
     */
    CONNECTION_REMOVED,

    /**
     * A node was added to the graph.
     * This may introduce new entry points or execution paths.
     */
    NODE_ADDED,

    /**
     * A node was removed from the graph.
     * This may invalidate connections and execution paths.
     */
    NODE_REMOVED
}
