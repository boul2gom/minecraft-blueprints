package fr.boul2gom.blueprints.nodes.flow;

import fr.boul2gom.blueprints.api.exception.validation.ValidationException;
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
 * Flow control node for iteration with a loop.
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 * - firstIndex (INTEGER): Starting index (inclusive)
 * - lastIndex (INTEGER): Ending index (inclusive)
 *
 * Output pins:
 * - loopBody (EXECUTION_FLOW): Executed for each iteration
 * - index (INTEGER): Current loop index
 * - completed (EXECUTION_FLOW): Executed after loop completes
 *
 * Loop mechanics:
 * - Uses context.getIterations() to track current iteration
 * - currentIndex = firstIndex + iterations
 * - If currentIndex <= lastIndex: execute loopBody and loop back
 * - Otherwise: execute completed and exit loop
 */
public class ForLoopNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("for_loop", "For Loop")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("first_index", "First Index", PinType.INTEGER)
            .input("last_index", "Last Index", PinType.INTEGER)
            .output("loop_body", "Loop Body", PinType.EXECUTION_FLOW)
            .output("index", "Index", PinType.INTEGER)
            .output("completed", "Completed", PinType.EXECUTION_FLOW);

        return new ForLoopNode(config, position);
    };

    private ForLoopNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        final IBlueprintPin first = this.getInput("first_index");
        final IBlueprintPin last = this.getInput("last_index");

        if (first == null || !first.isConnected()) {
            throw new ValidationException("ForLoop node requires 'first_index' input to be connected");
        }

        if (last == null || !last.isConnected()) {
            throw new ValidationException("ForLoop node requires 'last_index' input to be connected");
        }
    }

    @Override
    public boolean is_loop_node() {
        return true;
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin first_pin = this.getInput("first_index");
        final IBlueprintPin last_pin = this.getInput("last_index");

        final Object first_value = context.get_pin_value(first_pin);
        final Object last_value = context.get_pin_value(last_pin);

        // Runtime validation: ensure values are numbers
        if (first_value == null) {
            throw new IllegalStateException(
                String.format("ForLoop node '%s': first_index has no value", this.getName())
            );
        }
        if (!(first_value instanceof Number)) {
            throw new IllegalStateException(
                String.format("ForLoop node '%s': first_index must be a number, got %s",
                    this.getName(), first_value.getClass().getSimpleName())
            );
        }

        if (last_value == null) {
            throw new IllegalStateException(
                String.format("ForLoop node '%s': last_index has no value", this.getName())
            );
        }
        if (!(last_value instanceof Number)) {
            throw new IllegalStateException(
                String.format("ForLoop node '%s': last_index must be a number, got %s",
                    this.getName(), last_value.getClass().getSimpleName())
            );
        }

        final int first_index = ((Number) first_value).intValue();
        final int last_index = ((Number) last_value).intValue();

        // Runtime validation: ensure range is reasonable (prevent memory exhaustion)
        final long range = (long) last_index - (long) first_index + 1;
        if (range < 0) {
            throw new IllegalStateException(
                String.format("ForLoop node '%s': invalid range [%d, %d] (first_index > last_index)",
                    this.getName(), first_index, last_index)
            );
        }
        if (range > 1000000) { // Max 1 million iterations
            throw new IllegalStateException(
                String.format("ForLoop node '%s': range too large [%d, %d] = %d iterations (max 1,000,000)",
                    this.getName(), first_index, last_index, range)
            );
        }

        // Get current iteration count (0-based)
        // First iteration: getIterations() returns 0, index = first_index
        // Second iteration: getIterations() returns 1, index = first_index + 1
        // Counter incremented by executor AFTER this method completes
        final int iteration_index = context.getIterations(this);
        final int current_index = first_index + iteration_index;

        // Output current index - use set_pin_value_for_next_frame() to ensure
        // the index is accessible in the loop body frame, not the parent frame
        final IBlueprintPin index_output = this.getOutput("index");
        context.set_pin_value_for_next_frame(index_output, current_index);

        // Check if we should continue looping
        if (current_index <= last_index) {
            // Frame push/pop is handled by BlueprintExecutor for isolation
            // Each iteration runs in a fresh execution frame
            return CompletableFuture.completedFuture(Set.of("loop_body"));
        } else {
            // Loop finished: reset iteration counter and execute completed
            context.reset_iterations(this);
            return CompletableFuture.completedFuture(Set.of("completed"));
        }
    }
}
