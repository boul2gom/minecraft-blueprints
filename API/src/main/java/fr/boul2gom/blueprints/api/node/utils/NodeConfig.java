package fr.boul2gom.blueprints.api.node.utils;

import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the configuration for a node, including its ID, name, input pins, and output pins.
 * This is used to define the structure of a node, specifying its inputs and outputs
 * with their respective identifiers, names, and types.
 *
 * Instances of this class are immutable with respect to their ID and name fields, while
 * allowing modification of the input and output pin definitions via the provided methods.
 */
public class NodeConfig {

    /** The unique identifier of the node. */
    private final String id;
    /** The name of the node. */
    private final String name;
    /** The list of input pins. */
    private final List<PinDefinition> inputs;
    /** The list of output pins. */
    private final List<PinDefinition> outputs;

    /**
     * Constructs a new {@code NodeConfig} instance with the specified node ID and name.
     * The ID and name are essential properties that uniquely identify and describe the node.
     * Both the ID and name must be non-null values.
     *
     * @param id   the unique identifier of the node, which must not be null
     * @param name the name of the node, which must not be null
     * @throws NullPointerException if {@code id} or {@code name} is null
     */
    public NodeConfig(String id, String name) {
        Objects.requireNonNull(id, "Node ID may not be null");
        Objects.requireNonNull(name, "Node name may not be null");

        this.id = id;
        this.name = name;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }

    /**
     * Adds an input pin to the node configuration.
     * @param id   the unique identifier of the input pin.
     * @param name the name of the input pin.
     * @param type the type of the input pin.
     * @return the current {@code NodeConfig} instance, allowing method chaining
     */
    public NodeConfig input(String id, String name, PinType type) {
        this.inputs.add(new PinDefinition(id, name, type));
        return this;
    }

    /**
     * Adds an output pin to the node configuration.
     *
     * @param id   the unique identifier of the output pin, which must not be null or blank
     * @param name the name of the output pin, which must not be null or blank
     * @param type the type of the output pin, which must not be null
     * @return the current {@code NodeConfig} instance, allowing method chaining
     */
    public NodeConfig output(String id, String name, PinType type) {
        this.outputs.add(new PinDefinition(id, name, type));
        return this;
    }

    /**
     * Gets the unique identifier of the node.
     * @return the unique identifier of the node.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the name of the node.
     * @return the name of the node.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the list of input pins.
     * @return the list of input pins.
     */
    public List<PinDefinition> getInputs() {
        return Collections.unmodifiableList(this.inputs);
    }

    /**
     * Gets the list of output pins.
     * @return the list of output pins.
     */
    public List<PinDefinition> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
    }

    @Override
    public String toString() {
        return String.format("NodeConfig(id=%s, name=%s, inputs=%d, outputs=%d)",
            this.id, this.name, this.inputs.size(), this.outputs.size());
    }
}
