package fr.boul2gom.blueprints.graph.observer;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.graph.observer.IGraphObserver;
import fr.boul2gom.blueprints.api.graph.observer.IGraphTopologyEvent;

/**
 * Observer that automatically revalidates a graph when its topology changes.
 * This ensures structural integrity is maintained after modifications.
 *
 * Validation is performed asynchronously to avoid blocking the topology change.
 * Validation errors are logged but do not prevent the modification.
 */
public class ValidationObserver implements IGraphObserver {

    @Override
    public void on_topology_changed(IGraphTopologyEvent event) {
        try {
            // Revalidate graph after topology change
            event.graph().validate();

            MinecraftBlueprints.LOGGER.debug(
                "Graph revalidated after {}: {}",
                event.type(),
                event.context() != null ? event.context() : "no context"
            );

        } catch (ValidationException e) {
            // Log validation failure but don't throw (allow modification to complete)
            MinecraftBlueprints.LOGGER.warn(
                "Graph validation failed after {}: {}",
                event.type(),
                e.getMessage()
            );
        }
    }
}
