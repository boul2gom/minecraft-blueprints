package fr.boul2gom.blueprints.nodes.variable;

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
 * Action node that writes a variable to the execution context.
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 * - name (STRING): The name of the variable to write
 * - value (STRING): The value to write
 *
 * Output pins:
 * - then (EXECUTION_FLOW): Execution output
 */
public class SetVariableNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("set_variable", "Set Variable")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("name", "Name", PinType.STRING)
            .input("value", "Value", PinType.STRING)
            .output("then", "Then", PinType.EXECUTION_FLOW);

        return new SetVariableNode(config, position);
    };

    private SetVariableNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        final IBlueprintPin name = this.getInput("name");
        if (name == null || !name.isConnected()) {
            throw new IllegalStateException("SetVariable node requires 'name' input to be connected");
        }

        final IBlueprintPin value = this.getInput("value");
        if (value == null || !value.isConnected()) {
            throw new IllegalStateException("SetVariable node requires 'value' input to be connected");
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Get the variable name and value from connected pins
        final IBlueprintPin name_pin = this.getInput("name");
        final IBlueprintPin value_pin = this.getInput("value");

        final Object name_value = context.get_pin_value(name_pin);
        final Object value_value = context.get_pin_value(value_pin);

        final String variable_name = name_value != null ? name_value.toString() : "";

        // Set the variable in context
        context.getVariables().set(variable_name, value_value);

        // Continue execution
        return CompletableFuture.completedFuture(Set.of("then"));
    }
}
