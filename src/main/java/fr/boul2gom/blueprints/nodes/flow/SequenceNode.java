package fr.boul2gom.blueprints.nodes.flow;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Flow control node that executes multiple output branches in sequence.
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 *
 * Output pins:
 * - then0 (EXECUTION_FLOW): First output
 * - then1 (EXECUTION_FLOW): Second output
 * - then2 (EXECUTION_FLOW): Third output
 *
 * NOTE: Current BFS executor doesn't guarantee execution order.
 * All connected outputs will execute, but order is not guaranteed.
 * TODO: Consider implementing DFS or ordered execution for sequence nodes
 */
public class SequenceNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("sequence", "Sequence")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("then0", "Then 0", PinType.EXECUTION_FLOW)
            .output("then1", "Then 1", PinType.EXECUTION_FLOW)
            .output("then2", "Then 2", PinType.EXECUTION_FLOW);

        return new SequenceNode(config, position);
    };

    private SequenceNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        // No special validation needed
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Sequence node returns all output pins
        // The executor will follow all of them (order preserved in Phase 2)
        return CompletableFuture.completedFuture(Set.of("then0", "then1", "then2"));
    }
}
