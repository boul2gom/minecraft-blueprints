package fr.boul2gom.blueprints.nodes.math;

import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.PinType;

/**
 * Pure node that divides two numbers.
 *
 * Input pins:
 * - a (FLOAT): Dividend
 * - b (FLOAT): Divisor
 *
 * Output pins:
 * - result (FLOAT): The quotient of a / b
 *
 * This is a pure node (no execution pins), so it's evaluated on-demand.
 * Division by zero returns 0.0.
 */
public class DivideNode extends BinaryOperationNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("divide", "Divide")
            .input("a", "A", PinType.FLOAT)
            .input("b", "B", PinType.FLOAT)
            .output("result", "Result", PinType.FLOAT);

        return new DivideNode(config, position);
    };

    private DivideNode(NodeConfig config, NodePosition position) {
        super(config, position, (a, b) -> b != 0.0 ? a / b : 0.0, "Divide");
    }
}
