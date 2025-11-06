package fr.boul2gom.blueprints.nodes.action;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.node.BlueprintNode;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.node.NodePosition;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Action node that schedules delayed execution.
 *
 * Input pins:
 * - exec (EXECUTION_FLOW): Execution input
 * - duration_ticks (INTEGER): Number of ticks to delay (20 ticks = 1 second)
 *
 * Output pins:
 * - completed (EXECUTION_FLOW): Executed after delay completes
 *
 * Implementation:
 * - Uses BlueprintScheduler to schedule execution N ticks in the future
 * - Returns CompletableFuture that completes after the delay
 * - Execution continues on server thread after delay expires
 */
public class DelayNode extends BlueprintNode {

    public static final NodeFactory FACTORY = (NodePosition position) -> {
        final NodeConfig config = new NodeConfig("delay", "Delay")
            .input("exec", "Exec", PinType.EXECUTION_FLOW)
            .input("duration_ticks", "Duration (Ticks)", PinType.INTEGER)
            .output("completed", "Completed", PinType.EXECUTION_FLOW);

        return new DelayNode(config, position);
    };

    private DelayNode(NodeConfig config, NodePosition position) {
        super(config, position);
    }

    @Override
    public void validate() {
        final IBlueprintPin duration = this.getInput("duration_ticks");
        if (duration == null || !duration.isConnected()) {
            throw new IllegalStateException("Delay node requires 'duration_ticks' input to be connected");
        }
    }

    @Override
    public CompletableFuture<Set<String>> execute(IExecutionContext context) {
        final IBlueprintPin duration_pin = this.getInput("duration_ticks");
        final Object duration_value = context.get_pin_value(duration_pin);

        final int duration_ticks = duration_value instanceof Number num ? num.intValue() : 20;
        final Executor executor = MinecraftBlueprints.INSTANCE.getServer();

        // Schedule delayed execution using BlueprintScheduler
        return IBlueprintScheduler.INSTANCE
            .schedule(duration_ticks, () -> {
                // This runnable executes after the delay
                // No action needed, just complete the future
            })
            .thenApplyAsync(v -> {
                // After delay completes, activate "completed" pin
                return Set.of("completed");
            }, executor);
    }
}
