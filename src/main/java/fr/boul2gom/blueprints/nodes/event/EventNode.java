package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for all event nodes.
 * Event nodes are entry points in blueprint graphs and are triggered by Fabric events.
 *
 * Characteristics:
 * - No execution input pins (events are entry points)
 * - Must have at least one execution output pin
 * - Have a unique event ID for indexing
 */
public abstract class EventNode extends BlueprintNode {

    protected EventNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    /**
     * Gets the unique event identifier for this node.
     * Used to match Fabric events to blueprint nodes.
     *
     * @return the event ID (e.g., "on_block_use", "on_server_tick")
     */
    public abstract String get_event_id();

    @Override
    public void validate() {
        // Event nodes must not have execution input pins
        for (final IBlueprintPin input : this.getInputs()) {
            if (input.isExecution()) {
                throw new IllegalStateException("Event node '" + this.getName() + "' cannot have execution input pins");
            }
        }

        // Event nodes must have at least one execution output pin
        boolean has_execution_output = false;
        for (final IBlueprintPin output : this.getOutputs()) {
            if (output.isExecution()) {
                has_execution_output = true;
                break;
            }
        }

        if (!has_execution_output) {
            throw new IllegalStateException("Event node '" + this.getName() + "' must have at least one execution output pin");
        }
    }
}
