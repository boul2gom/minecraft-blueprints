package fr.boul2gom.blueprints.nodes.math;

import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

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
public class MultiplyNode extends BinaryMathNode {

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
    protected double compute(double a, double b) {
        return a * b;
    }
}
