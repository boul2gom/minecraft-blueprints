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
 * Pure node that reads a variable from the execution context.
 *
 * Input pins:
 * - name (STRING): The name of the variable to read
 *
 * Output pins:
 * - value (STRING): The value of the variable (as string)
 *
 * This is a pure node (no execution pins), so it's evaluated on-demand
 * when its output is needed by another node.
 */
public class GetVariableNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("get_variable", "Get Variable")
            .input("name", "Name", PinType.STRING)
            .output("value", "Value", PinType.STRING);

        return new GetVariableNode(config, position);
    };

    private GetVariableNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        require_connected("name");
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Get the variable name from the connected pin
        final IBlueprintPin name_pin = this.getInput("name");
        final Object name_value = context.get_pin_value(name_pin);

        final String variable_name = name_value != null ? name_value.toString() : "";

        // Read the variable from context
        final Object variable_value = context.getVariables().get(variable_name);

        // Set the output pin value
        final IBlueprintPin value_output = this.getOutput("value");
        context.set_pin_value(value_output, variable_value != null ? variable_value.toString() : null);

        // Pure node - no execution pins to follow
        return CompletableFuture.completedFuture(Set.of());
    }
}
