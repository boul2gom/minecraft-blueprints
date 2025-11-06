package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper utility for setting event data on event node output pins.
 * Provides a fluent builder API for setting multiple pin values at once.
 */
public class EventNodeHelper {

    /**
     * Sets event data on event node output pins from a map.
     *
     * @param event_node The event node
     * @param context The execution context
     * @param data Map of output pin IDs to values
     */
    public static void set_event_data(
        IBlueprintNode event_node,
        IExecutionContext context,
        Map<String, Object> data
    ) {
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            final String pin_id = entry.getKey();
            final Object value = entry.getValue();

            final IBlueprintPin output_pin = event_node.getOutput(pin_id);
            if (output_pin != null) {
                context.set_pin_value(output_pin, value);
            } else {
                MinecraftBlueprints.LOGGER.warn(
                    "Event node '{}' has no output pin '{}'",
                    event_node.getName(),
                    pin_id
                );
            }
        }
    }

    /**
     * Creates a builder for fluently setting event data.
     *
     * @param event_node The event node
     * @param context The execution context
     * @return A new EventDataBuilder
     */
    public static EventDataBuilder builder(IBlueprintNode event_node, IExecutionContext context) {
        return new EventDataBuilder(event_node, context);
    }

    /**
     * Builder for fluently setting event data on event node output pins.
     */
    public static class EventDataBuilder {
        private final IBlueprintNode event_node;
        private final IExecutionContext context;
        private final Map<String, Object> data;

        private EventDataBuilder(IBlueprintNode event_node, IExecutionContext context) {
            this.event_node = event_node;
            this.context = context;
            this.data = new HashMap<>();
        }

        /**
         * Sets a pin value.
         *
         * @param pin_id The output pin ID
         * @param value The value to set
         * @return This builder for chaining
         */
        public EventDataBuilder pin(String pin_id, Object value) {
            this.data.put(pin_id, value);
            return this;
        }

        /**
         * Applies all pin values to the event node.
         */
        public void apply() {
            EventNodeHelper.set_event_data(this.event_node, this.context, this.data);
        }
    }
}
