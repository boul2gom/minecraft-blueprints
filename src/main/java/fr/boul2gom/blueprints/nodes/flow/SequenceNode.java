package fr.boul2gom.blueprints.nodes.flow;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.LinkedHashSet;
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
 */
public class SequenceNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("sequence", "Sequence")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .output("then0", "Then 0", PinType.EXECUTION_FLOW)
            .output("then1", "Then 1", PinType.EXECUTION_FLOW);

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
        // Sequence node returns all output pins in order
        // LinkedHashSet preserves insertion order: then0 executes before then1
        final Set<String> ordered_pins = new LinkedHashSet<>();
        ordered_pins.add("then0");
        ordered_pins.add("then1");
        return CompletableFuture.completedFuture(ordered_pins);
    }
}
