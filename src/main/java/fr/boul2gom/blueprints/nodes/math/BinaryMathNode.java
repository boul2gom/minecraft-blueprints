package fr.boul2gom.blueprints.nodes.math;

import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for binary mathematical operations.
 * Eliminates code duplication across Add, Subtract, Multiply, Divide nodes.
 *
 * All binary math nodes have:
 * - Two input pins: a (number), b (number)
 * - One output pin: result (number)
 * - Pure evaluation (no execution pins)
 */
public abstract class BinaryMathNode extends BlueprintNode {

    protected BinaryMathNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    /**
     * Performs the mathematical operation on two numbers.
     * Implemented by subclasses (Add, Subtract, Multiply, Divide).
     *
     * @param a The first operand
     * @param b The second operand
     * @return The result of the operation
     */
    protected abstract double compute(double a, double b);

    @Override
    public void validate() {
        final IBlueprintPin a = this.getInput("a");
        final IBlueprintPin b = this.getInput("b");

        if (a == null || !a.isConnected()) {
            throw new ValidationException(String.format("%s node requires 'a' input to be connected", this.getName()));
        }

        if (b == null || !b.isConnected()) {
            throw new ValidationException(String.format("%s node requires 'b' input to be connected", this.getName()));
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin a_pin = this.getInput("a");
        final IBlueprintPin b_pin = this.getInput("b");

        final Object a_value = context.get_pin_value(a_pin);
        final Object b_value = context.get_pin_value(b_pin);

        // Convert to numbers (default to 0 if not a number)
        final double a = a_value instanceof Number num ? num.doubleValue() : 0.0;
        final double b = b_value instanceof Number num ? num.doubleValue() : 0.0;

        // Perform operation
        final double result = this.compute(a, b);

        // Set output pin value
        final IBlueprintPin result_pin = this.getOutput("result");
        context.set_pin_value(result_pin, result);

        // Pure node: no execution flow
        return CompletableFuture.completedFuture(Set.of());
    }
}
