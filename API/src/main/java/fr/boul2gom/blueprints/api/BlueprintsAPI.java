package fr.boul2gom.blueprints.api;

import fr.boul2gom.blueprints.api.exception.ProviderNotRegisteredException;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import fr.boul2gom.blueprints.api.node.NodeFactory;
import fr.boul2gom.blueprints.api.provider.ProviderRegistry;

/**
 * The BlueprintsAPI is the core interface for the Minecraft Blueprints mod, providing
 * a visual scripting system inspired by Unreal Engine's Blueprint system.
 *
 * <h2>System Architecture</h2>
 *
 * The Blueprints system consists of several key components:
 *
 * <h3>1. Graph Structure</h3>
 * <ul>
 *   <li><b>Nodes</b> ({@link fr.boul2gom.blueprints.api.node.IBlueprintNode}): Executable units of logic</li>
 *   <li><b>Pins</b> ({@link fr.boul2gom.blueprints.api.pin.IBlueprintPin}): Connection points for data and execution flow</li>
 *   <li><b>Connections</b> ({@link fr.boul2gom.blueprints.api.connection.IBlueprintConnection}): Links between pins</li>
 *   <li><b>Graphs</b> ({@link fr.boul2gom.blueprints.api.graph.IBlueprintGraph}): Complete blueprint definitions</li>
 * </ul>
 *
 * <h3>2. Execution Model</h3>
 * <p>
 * Blueprints execute using async-aware DAG traversal with CompletableFuture:
 * </p>
 * <ul>
 *   <li><b>Entry Points</b>: Event nodes triggered by Minecraft events</li>
 *   <li><b>Execution Flow</b>: White execution pins control node execution order</li>
 *   <li><b>Data Flow</b>: Colored data pins pass values between nodes</li>
 *   <li><b>Pure Nodes</b>: Nodes without execution pins, evaluated on-demand</li>
 *   <li><b>Loop Support</b>: Iteration tracking with frame isolation</li>
 * </ul>
 *
 * <h3>3. Safety & Performance</h3>
 * <p>
 * The execution engine enforces strict safety limits to prevent server lag:
 * </p>
 * <ul>
 *   <li><b>Max Execution Time</b>: 50ms per blueprint (1 server tick)</li>
 *   <li><b>Max Nodes</b>: 10,000 nodes per execution</li>
 *   <li><b>Max Loop Iterations</b>: 10,000 iterations per loop</li>
 *   <li><b>Cycle Detection</b>: Validates graphs before execution</li>
 * </ul>
 *
 * <h3>4. Provider Pattern</h3>
 * <p>
 * The API uses lazy-initialized singletons via {@link ProviderRegistry}:
 * </p>
 * <ul>
 *   <li>{@link IBlueprintExecutor#INSTANCE}: Executes blueprints</li>
 *   <li>{@link IBlueprintScheduler#INSTANCE}: Schedules delayed tasks</li>
 * </ul>
 *
 * <h2>Creating Custom Nodes</h2>
 *
 * <p>Third-party mods can extend the system by implementing {@link fr.boul2gom.blueprints.api.node.IBlueprintNode}:</p>
 *
 * <pre>{@code
 * public class CustomNode implements IBlueprintNode {
 *     public static final NodeFactory FACTORY = (position) -> {
 *         NodeConfig config = new NodeConfig("custom_node", "Custom Node")
 *             .input("exec", "Exec", PinType.EXECUTION_FLOW)
 *             .input("value", "Value", PinType.INTEGER)
 *             .output("then", "Then", PinType.EXECUTION_FLOW);
 *         return new CustomNode(config, position);
 *     };
 *
 *     @Override
 *     public CompletableFuture<Set<String>> execute(IExecutionContext context) {
 *         int value = context.get_pin_value(getInput("value"));
 *         // ... custom logic ...
 *         return CompletableFuture.completedFuture(Set.of("then"));
 *     }
 * }
 * }</pre>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 *   <li><b>BlueprintExecutor</b>: Thread-safe, can execute multiple blueprints concurrently</li>
 *   <li><b>ExecutionContext</b>: NOT thread-safe, one per execution</li>
 *   <li><b>Nodes</b>: Stateless, reused across executions</li>
 *   <li><b>Graphs</b>: Read-only during execution, modifications require validation</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>
 * The system uses dedicated exceptions for different failure modes:
 * </p>
 * <ul>
 *   <li><b>ValidationException</b>: Graph structure issues (cycles, invalid connections)</li>
 *   <li><b>ExecutionException</b>: Runtime errors during node execution</li>
 *   <li><b>ExecutionTimeoutException</b>: Blueprint exceeded time/node limits</li>
 *   <li><b>ProviderNotRegisteredException</b>: Missing provider registration</li>
 * </ul>
 *
 * @see IBlueprintExecutor
 * @see IBlueprintScheduler
 * @see fr.boul2gom.blueprints.api.node.IBlueprintNode
 * @see fr.boul2gom.blueprints.api.graph.IBlueprintGraph
 */
public interface BlueprintsAPI {

}
