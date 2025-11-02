package fr.boul2gom.blueprints.nodes;

import fr.boul2gom.blueprints.api.node.*;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

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
    public void execute() {
        // TODO: Implement execution logic
    }
}
