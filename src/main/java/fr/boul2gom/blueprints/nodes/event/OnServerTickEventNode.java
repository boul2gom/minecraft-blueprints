package fr.boul2gom.blueprints.nodes.event;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Event node triggered at the end of every server tick.
 *
 * Output pins:
 * - exec (EXECUTION_FLOW): Execution flow output
 * - server (STRING): The server instance (stored as string identifier)
 *
 * Warning: This event fires every tick (20 times per second).
 * Blueprints using this node should be lightweight to avoid server lag.
 */
public class OnServerTickEventNode extends EventNode {

    public static final String EVENT_ID = "on_server_tick";

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("on_server_tick", "On Server Tick")
            .output("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("server", "Server", PinType.STRING);

        return new OnServerTickEventNode(config, position);
    };

    private OnServerTickEventNode(NodeConfig config, NodePosition position) {
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
