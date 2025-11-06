package fr.boul2gom.blueprints.graph.observer;

import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.graph.observer.IGraphTopologyEvent;
import fr.boul2gom.blueprints.api.graph.observer.TopologyEventType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * Immutable record representing a topology change event.
 * Created when connections or nodes are added/removed from a graph.
 */
public record GraphTopologyEvent(
        IBlueprintGraph graph,
        TopologyEventType type,
        @Nullable String context,
        Instant timestamp
) implements IGraphTopologyEvent { }
