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
     *         - Empty set or null: follow all execution output pins (backward compatibility)
     *         - Non-execution pins are not included in the returned set
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
}
