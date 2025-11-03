package fr.boul2gom.blueprints.nodes.flow;

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
            throw new IllegalStateException("ForLoop node requires 'first_index' input to be connected");
        }

        if (last == null || !last.isConnected()) {
            throw new IllegalStateException("ForLoop node requires 'last_index' input to be connected");
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin first_pin = this.getInput("first_index");
        final IBlueprintPin last_pin = this.getInput("last_index");

        final Object first_value = context.get_pin_value(first_pin);
        final Object last_value = context.get_pin_value(last_pin);

        final int first_index = first_value instanceof Number num ? num.intValue() : 0;
        final int last_index = last_value instanceof Number num ? num.intValue() : 0;

        // Calculate current index based on iteration count
        // Note: getIterations returns count BEFORE increment (executor increments before execute)
        // So iteration 0 means this is the SECOND execution (first was iteration -1... wait no)
        // Actually: executor increments BEFORE calling execute, so getIterations() returns the current count
        // So we need to subtract 1 to get the iteration number
        final int iterations = context.getIterations(this) - 1;
        final int current_index = first_index + iterations;

        // Output current index
        final IBlueprintPin index_output = this.getOutput("index");
        context.set_pin_value(index_output, current_index);

        // Check if we should continue looping
        if (current_index <= last_index) {
            // Continue loop: execute loopBody
            return CompletableFuture.completedFuture(Set.of("loop_body"));
        } else {
            // Loop finished: execute completed
            return CompletableFuture.completedFuture(Set.of("completed"));
        }
    }
}
