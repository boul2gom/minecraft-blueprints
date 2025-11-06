package fr.boul2gom.blueprints.nodes.flow;

import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;
import fr.boul2gom.blueprints.util.TypeConverter;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Flow control node for conditional branching (if/else).
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 * - condition (BOOLEAN): The condition to evaluate
 *
 * Output pins:
 * - true (EXECUTION_FLOW): Executed if condition is true
 * - false (EXECUTION_FLOW): Executed if condition is false
 *
 * This node returns the active pin based on the condition evaluation.
 * Only the selected branch will execute.
 */
public class BranchNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("branch", "Branch")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("condition", "Condition", PinType.BOOLEAN)
            .output("true", "True", PinType.EXECUTION_FLOW)
            .output("false", "False", PinType.EXECUTION_FLOW);

        return new BranchNode(config, position);
    };

    private BranchNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        require_connected("condition");
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin condition_pin = this.getInput("condition");
        final Object condition_value = context.get_pin_value(condition_pin);

        // Evaluate condition with strict type validation (control flow requires explicit boolean)
        final boolean condition = TypeConverter.require_boolean(
            condition_value,
            String.format("Branch node '%s' condition pin", this.getName())
        ).booleanValue();

        // Return the appropriate pin to follow based on condition
        final String active_pin = condition ? "true" : "false";
        return CompletableFuture.completedFuture(Set.of(active_pin));
    }
}
