package fr.boul2gom.blueprints.nodes.action;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PrintNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("print", "Print")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("message", "Message", PinType.STRING)
            .output("then", "Then", PinType.EXECUTION_FLOW);

        return new PrintNode(config, position);
    };

    private PrintNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        // Validation: message input must be connected
        final IBlueprintPin message = this.getInput("message");
        if (message == null) {
            throw new IllegalStateException("Print node requires 'message' input");
        }

        if (!message.isConnected()) {
            throw new IllegalStateException("Print node requires 'message' input to be connected");
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Get the message value from the connected pin using data flow resolution
        final IBlueprintPin message_pin = this.getInput("message");
        final Object message_value = context.get_pin_value(message_pin);

        // Print the message
        if (message_value != null) {
            System.out.println("[Blueprint Print] " + message_value);
        } else {
            System.out.println("[Blueprint Print] (null)");
        }

        // Continue execution
        return CompletableFuture.completedFuture(Set.of("then"));
    }
}
