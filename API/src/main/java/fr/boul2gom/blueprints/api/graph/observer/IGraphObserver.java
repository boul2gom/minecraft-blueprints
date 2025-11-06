package fr.boul2gom.blueprints.api.graph.observer;

/**
 * Observer that listens to topology changes in a blueprint graph.
 * Implementing classes can react to structural changes such as:
 * - Connection additions/removals
 * - Node additions/removals
 *
 * This follows the Observer pattern to decouple graph modifications
 * from validation logic, allowing automatic revalidation without
 * tight coupling between components.
 */
@FunctionalInterface
public interface IGraphObserver {

    /**
     * Called when a topology change occurs in the observed graph.
     *
     * @param event the topology change event with details about what changed
     */
    void on_topology_changed(IGraphTopologyEvent event);
}
