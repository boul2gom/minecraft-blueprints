package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Event node triggered when a player right-clicks a block.
 *
 * Output pins:
 * - exec (EXECUTION_FLOW): Execution flow output
 * - player (ENTITY): The player who used the block
 * - blockPos (BLOCK_POS): The position of the block
 * - world (WORLD): The world where the event occurred
 * - hand (STRING): The hand used (MAIN_HAND or OFF_HAND)
 */
public class OnBlockUseEventNode extends EventNode {

    public static final String EVENT_ID = "on_block_use";

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("on_block_use", "On Block Use")
            .output("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("player", "Player", PinType.ENTITY)
            .output("block_pos", "Block Pos", PinType.BLOCK_POS)
            .output("world", "World", PinType.WORLD)
            .output("hand", "Hand", PinType.STRING);

        return new OnBlockUseEventNode(config, position);
    };

    private OnBlockUseEventNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public String get_event_id() {
        return EVENT_ID;
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Event nodes typically don't execute logic - they just provide data
        // Event data is set by the event handler before execution starts

        // The output pin values should be set by the event handler:
        // context.set_pin_value(getOutput("player"), player)
        // context.set_pin_value(getOutput("blockPos"), blockPos)
        // context.set_pin_value(getOutput("world"), world)
        // context.set_pin_value(getOutput("hand"), hand.name())

        // Return the execution pin to follow
        return CompletableFuture.completedFuture(Set.of("exec"));
    }
}
