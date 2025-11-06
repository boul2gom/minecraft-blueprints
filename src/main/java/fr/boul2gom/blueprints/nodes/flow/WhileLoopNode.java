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
 * Flow control node for conditional looping.
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 * - condition (BOOLEAN): Loop condition (evaluated each iteration)
 *
 * Output pins:
 * - loopBody (EXECUTION_FLOW): Executed while condition is true
 * - completed (EXECUTION_FLOW): Executed when condition becomes false
 *
 * Loop mechanics:
 * - Evaluates condition on each execution
 * - If true: executes loopBody and loops back
 * - If false: executes completed and exits loop
 * - Protected by MAX_ITERATIONS_PER_LOOP to prevent infinite loops
 */
public class WhileLoopNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("while_loop", "While Loop")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("condition", "Condition", PinType.BOOLEAN)
            .output("loop_body", "Loop Body", PinType.EXECUTION_FLOW)
            .output("completed", "Completed", PinType.EXECUTION_FLOW);

        return new WhileLoopNode(config, position);
    };

    private WhileLoopNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        require_connected("condition");
    }

    @Override
    public boolean is_loop_node() {
        return true;
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        // Iteration limits are enforced by BlueprintExecutor.execute_node() (line 202-207)
        // Each loop iteration increments the counter; executor throws ExecutionException if limit exceeded

        final IBlueprintPin condition_pin = this.getInput("condition");
        final Object condition_value = context.get_pin_value(condition_pin);

        // Strict type validation for control flow (while loop requires explicit boolean)
        final boolean condition = TypeConverter.require_boolean(
            condition_value,
            String.format("WhileLoop node '%s' condition pin", this.getName())
        ).booleanValue();

        if (condition) {
            // Frame push/pop is handled by BlueprintExecutor for isolation
            // Each iteration runs in a fresh execution frame
            return CompletableFuture.completedFuture(Set.of("loop_body"));
        } else {
            // Condition is false: exit loop and reset iteration counter
            context.reset_iterations(this);
            return CompletableFuture.completedFuture(Set.of("completed"));
        }
    }
}
