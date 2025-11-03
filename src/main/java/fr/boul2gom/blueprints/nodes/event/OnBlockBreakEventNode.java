package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Event node triggered when a player breaks a block.
 *
 * Output pins:
 * - exec (EXECUTION_FLOW): Execution flow output
 * - player (ENTITY): The player who broke the block
 * - blockPos (BLOCK_POS): The position of the broken block
 * - blockState (BLOCK_STATE): The state of the broken block
 * - world (WORLD): The world where the break occurred
 */
public class OnBlockBreakEventNode extends EventNode {

    public static final String EVENT_ID = "on_block_break";

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("on_block_break", "On Block Break")
            .output("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("player", "Player", PinType.ENTITY)
            .output("block_pos", "Block Pos", PinType.BLOCK_POS)
            .output("block_state", "Block State", PinType.BLOCK_STATE)
            .output("world", "World", PinType.WORLD);

        return new OnBlockBreakEventNode(config, position);
    };

    private OnBlockBreakEventNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public String get_event_id() {
        return EVENT_ID;
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Event nodes don't execute logic - data is set by the event handler
        return CompletableFuture.completedFuture(Set.of("exec"));
    }
}
