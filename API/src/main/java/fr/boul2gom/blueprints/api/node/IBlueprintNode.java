package fr.boul2gom.blueprints.api.node;

import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * The IBlueprintNode interface represents a node within a blueprint system.
 * Nodes are fundamental building blocks of a blueprint, encapsulating logic
 * or operations that can be executed in a defined order.
 *
 * Responsibilities of a blueprint node include:
 * - Managing its unique identifier and name for identification purposes.
 * - Managing input and output pins to define data flow connections with other nodes.
 * - Validating its configuration and linking correctness.
 * - Executing its core logic within an execution context.
 * - Managing its spatial position within a blueprint.
 */
public interface IBlueprintNode {

    /**
     * Get the unique identifier of the node.
     * @return unique identifier of the node.
     */
    String getId();

    /**
     * Get the name of the node.
     * @return name of the node.
     */
    String getName();

    /**
     * Get the list of input pins.
     * @return the list of input pins.
     */
    List<? extends IBlueprintPin> getInputs();

    /**
     * Get the list of output pins.
     * @return the list of output pins.
     */
    List<? extends IBlueprintPin> getOutputs();

    /**
     * Get the input pin with the given identifier.
     * @param id the identifier of the pin.
     * @return the pin, or null if not found.
     */
    @Nullable IBlueprintPin getInput(String id);

    /**
     * Get the output pin with the given identifier.
     * @param id the identifier of the pin.
     * @return the pin, or null if not found.
     */
    @Nullable IBlueprintPin getOutput(String id);

    /**
     * Validate the node configuration.
     */
    void validate();

    /**
     * Execute the node logic within the given execution context.
     *
     * @param context the execution context
     * @return a CompletableFuture that resolves to a Set of active output execution pin IDs.
     *         - For synchronous nodes: return CompletableFuture.completedFuture(Set.of("pin_id"))
     *         - For async nodes: return a future that completes later
     *         - null or empty set (Set.of()): stops execution (no output pins activated)
     *         - Non-execution pins are not included in the returned set
     *
     *         Example for branch node:
     *         - If condition true: return Set.of("true_branch")
     *         - If condition false: return Set.of("false_branch")
     *
     *         Example for terminal node:
     *         - return null or Set.of() to stop execution
     */
    CompletableFuture<Set<String>> execute(IExecutionContext context);

    /**
     * Get the spatial position of the node within the blueprint.
     * @return the spatial position of the node.
     */
    NodePosition getPosition();

    /**
     * Set the spatial position of the node within the blueprint.
     * @param position the spatial position of the node.
     */
    void setPosition(NodePosition position);

    /**
     * Returns whether this node is a loop node that creates iteration frames.
     * Loop nodes have their loop body executed in isolated execution frames
     * to prevent pin value pollution across iterations.
     *
     * @return true if this is a loop node (ForLoop, WhileLoop, etc.), false otherwise
     */
    default boolean is_loop_node() {
        return false;
    }
}
