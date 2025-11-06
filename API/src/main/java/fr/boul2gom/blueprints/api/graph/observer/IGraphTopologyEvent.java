package fr.boul2gom.blueprints.api.graph.observer;

import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Event fired when the topology of a blueprint graph changes.
 * Topology changes include adding/removing nodes or connections,
 * which may invalidate the graph structure and require revalidation.
 */
public interface IGraphTopologyEvent {

    /**
     * Get the graph that was modified.
     * @return the affected graph
     */
    IBlueprintGraph graph();

    /**
     * Get the type of topology change.
     * @return the event type
     */
    TopologyEventType type();

    /**
     * Get additional context about the change (e.g., node ID, pin IDs).
     * @return context string, or null if not applicable
     */
    @Nullable
    String context();

    /**
     * Get the timestamp when the event occurred.
     * @return the timestamp
     */
    Instant timestamp();
}
