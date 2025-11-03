package fr.boul2gom.blueprints.nodes.math;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Pure node that multiplies two numbers.
 *
 * Input pins:
 * - a (FLOAT): First operand
 * - b (FLOAT): Second operand
 *
 * Output pins:
 * - result (FLOAT): The product of a * b
 *
 * This is a pure node (no execution pins), so it's evaluated on-demand.
 */
public class MultiplyNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("multiply", "Multiply")
            .input("a", "A", PinType.FLOAT)
            .input("b", "B", PinType.FLOAT)
            .output("result", "Result", PinType.FLOAT);

        return new MultiplyNode(config, position);
    };

    private MultiplyNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        final IBlueprintPin a = this.getInput("a");
        final IBlueprintPin b = this.getInput("b");

        if (a == null || !a.isConnected()) {
            throw new IllegalStateException("Multiply node requires 'a' input to be connected");
        }

        if (b == null || !b.isConnected()) {
            throw new IllegalStateException("Multiply node requires 'b' input to be connected");
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin a_pin = this.getInput("a");
        final IBlueprintPin b_pin = this.getInput("b");

        final Object a_value = context.get_pin_value(a_pin);
        final Object b_value = context.get_pin_value(b_pin);

        final double a = a_value instanceof Number num ? num.doubleValue() : 0.0;
        final double b = b_value instanceof Number num ? num.doubleValue() : 0.0;

        final double result = a * b;

        final IBlueprintPin result_output = this.getOutput("result");
        context.set_pin_value(result_output, result);

        // Pure node - no execution pins
        return CompletableFuture.completedFuture(Set.of());
    }
}
