package fr.boul2gom.blueprints.api.node.utils;

import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NodeConfig {

    private final String id;
    private final String name;
    private final List<PinDefinition> inputs;
    private final List<PinDefinition> outputs;

    public NodeConfig(String id, String name) {
        Objects.requireNonNull(id, "Node ID may not be null");
        Objects.requireNonNull(name, "Node name may not be null");

        this.id = id;
        this.name = name;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }

    public NodeConfig input(String id, String name, PinType type) {
        this.inputs.add(new PinDefinition(id, name, type));
        return this;
    }

    public NodeConfig output(String id, String name, PinType type) {
        this.outputs.add(new PinDefinition(id, name, type));
        return this;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<PinDefinition> getInputs() {
        return Collections.unmodifiableList(this.inputs);
    }

    public List<PinDefinition> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
    }
}
