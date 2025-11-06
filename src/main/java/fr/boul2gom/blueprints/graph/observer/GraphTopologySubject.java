package fr.boul2gom.blueprints.graph.observer;

import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.graph.observer.IGraphObserver;
import fr.boul2gom.blueprints.api.graph.observer.IGraphTopologyEvent;
import fr.boul2gom.blueprints.api.graph.observer.TopologyEventType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject that manages observers and notifies them of topology changes.
 * Thread-safe using CopyOnWriteArrayList for concurrent modifications.
 */
public class GraphTopologySubject {

    private final IBlueprintGraph graph;
    private final List<IGraphObserver> observers;

    public GraphTopologySubject(IBlueprintGraph graph) {
        this.graph = graph;
        this.observers = new CopyOnWriteArrayList<>();
    }

    /**
     * Register an observer to receive topology change notifications.
     *
     * @param observer the observer to register
     */
    public void register(IGraphObserver observer) {
        if (observer != null && !this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Unregister an observer from topology change notifications.
     *
     * @param observer the observer to unregister
     */
    public void unregister(IGraphObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify all observers of a topology change event.
     *
     * @param type the type of topology change
     * @param context additional context about the change
     */
    public void notify_observers(TopologyEventType type, @Nullable String context) {
        final IGraphTopologyEvent event = new GraphTopologyEvent(this.graph, type, context, Instant.now());

        // Notify all observers (CopyOnWriteArrayList ensures thread safety)
        for (final IGraphObserver observer : this.observers) {
            try {
                observer.on_topology_changed(event);
            } catch (Exception e) {
                // Log error but continue notifying other observers
                System.err.println("Error notifying observer: " + e.getMessage());
            }
        }
    }

    /**
     * Get the number of registered observers.
     *
     * @return observer count
     */
    public int get_observer_count() {
        return this.observers.size();
    }
}
