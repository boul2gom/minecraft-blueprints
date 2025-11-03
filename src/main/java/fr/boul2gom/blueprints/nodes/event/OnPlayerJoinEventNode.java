package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Event node triggered when a player joins the server.
 *
 * Output pins:
 * - exec (EXECUTION_FLOW): Execution flow output
 * - player (ENTITY): The player who joined
 * - server (STRING): The server instance
 */
public class OnPlayerJoinEventNode extends EventNode {

    public static final String EVENT_ID = "on_player_join";

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("on_player_join", "On Player Join")
            .output("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("player", "Player", PinType.ENTITY)
            .output("server", "Server", PinType.STRING);

        return new OnPlayerJoinEventNode(config, position);
    };

    private OnPlayerJoinEventNode(NodeConfig config, NodePosition position) {
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
