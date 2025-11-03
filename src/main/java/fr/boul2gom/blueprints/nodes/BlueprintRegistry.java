package fr.boul2gom.blueprints.nodes;

import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.nodes.event.EventNode;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for blueprints.
 * Stores blueprints and indexes them by event type for efficient lookup when events fire.
 */
public class BlueprintRegistry {

    private static final Map<String, IBlueprintGraph> BLUEPRINTS = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> EVENT_TO_BLUEPRINTS = new ConcurrentHashMap<>();

    /**
     * Registers a blueprint in the registry.
     * Automatically indexes the blueprint by its event nodes.
     *
     * @param blueprint the blueprint to register
     */
    public static void register(IBlueprintGraph blueprint) {
        Objects.requireNonNull(blueprint, "Blueprint may not be null");

        final String blueprint_id = blueprint.getId();
        BLUEPRINTS.put(blueprint_id, blueprint);

        // Index by event types from entry point event nodes
        for (final IBlueprintNode node : blueprint.getNodes()) {
            if (node instanceof EventNode event_node) {
                final String event_id = event_node.get_event_id();
                EVENT_TO_BLUEPRINTS
                    .computeIfAbsent(event_id, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(blueprint_id);
            }
        }
    }

    /**
     * Unregisters a blueprint from the registry.
     *
     * @param blueprint_id the ID of the blueprint to unregister
     */
    public static void unregister(String blueprint_id) {
        Objects.requireNonNull(blueprint_id, "Blueprint ID may not be null");

        final IBlueprintGraph blueprint = BLUEPRINTS.remove(blueprint_id);
        if (blueprint == null) {
            return;
        }

        // Remove from event index
        for (final IBlueprintNode node : blueprint.getNodes()) {
            if (node instanceof EventNode event_node) {
                final String event_id = event_node.get_event_id();
                final List<String> blueprint_ids = EVENT_TO_BLUEPRINTS.get(event_id);
                if (blueprint_ids != null) {
                    blueprint_ids.remove(blueprint_id);
                    if (blueprint_ids.isEmpty()) {
                        EVENT_TO_BLUEPRINTS.remove(event_id);
                    }
                }
            }
        }
    }

    /**
     * Gets a blueprint by its ID.
     *
     * @param blueprint_id the blueprint ID
     * @return the blueprint, or null if not found
     */
    @Nullable
    public static IBlueprintGraph get(String blueprint_id) {
        Objects.requireNonNull(blueprint_id, "Blueprint ID may not be null");
        return BLUEPRINTS.get(blueprint_id);
    }

    /**
     * Gets all blueprints that listen to a specific event.
     *
     * @param event_id the event ID (e.g., "on_block_use")
     * @return list of blueprints that handle this event
     */
    public static List<IBlueprintGraph> get_blueprints_for_event(String event_id) {
        Objects.requireNonNull(event_id, "Event ID may not be null");

        final List<String> blueprint_ids = EVENT_TO_BLUEPRINTS.get(event_id);
        if (blueprint_ids == null || blueprint_ids.isEmpty()) {
            return Collections.emptyList();
        }

        // Resolve blueprint IDs to actual blueprints
        return blueprint_ids.stream()
            .map(BLUEPRINTS::get)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Gets all registered blueprints.
     *
     * @return collection of all blueprints
     */
    public static Collection<IBlueprintGraph> getAll() {
        return Collections.unmodifiableCollection(BLUEPRINTS.values());
    }

    /**
     * Gets all registered blueprint IDs.
     *
     * @return set of blueprint IDs
     */
    public static Set<String> get_all_ids() {
        return Collections.unmodifiableSet(BLUEPRINTS.keySet());
    }

    /**
     * Checks if a blueprint is registered.
     *
     * @param blueprint_id the blueprint ID
     * @return true if registered, false otherwise
     */
    public static boolean isRegistered(String blueprint_id) {
        return BLUEPRINTS.containsKey(blueprint_id);
    }

    /**
     * Clears all registered blueprints. Used for testing.
     */
    static void clear() {
        BLUEPRINTS.clear();
        EVENT_TO_BLUEPRINTS.clear();
    }
}
