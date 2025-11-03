package fr.boul2gom.blueprints.api.node;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.execution.IExecutionContext;
import fr.boul2gom.blueprints.api.node.utils.NodeConfig;
import fr.boul2gom.blueprints.api.node.utils.PinDefinition;
import fr.boul2gom.blueprints.api.pin.BlueprintPin;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinDirection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class BlueprintNode implements IBlueprintNode {

    private final String id;
    private final String name;
    private NodePosition position;

    private final List<IBlueprintPin> inputs;
    private final List<IBlueprintPin> outputs;

    protected BlueprintNode(NodeConfig config, NodePosition position) {
        Objects.requireNonNull(config, "Node config may not be null");
        Objects.requireNonNull(position, "Node position may not be null");

        this.id = config.getId();
        this.name = config.getName();
        this.position = position;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();

        // Initialize pins from config
        for (final PinDefinition definition : config.getInputs()) {
            final IBlueprintPin pin = new BlueprintPin(
                definition.id(),
                definition.name(),
                definition.type(),
                PinDirection.INPUT,
                this
            );
            this.inputs.add(pin);
        }

        for (final PinDefinition definition : config.getOutputs()) {
            final IBlueprintPin pin = new BlueprintPin(
                definition.id(),
                definition.name(),
                definition.type(),
                PinDirection.OUTPUT,
                this
            );
            this.outputs.add(pin);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<? extends IBlueprintPin> getInputs() {
        return Collections.unmodifiableList(this.inputs);
    }

    @Override
    public List<? extends IBlueprintPin> getOutputs() {
        return Collections.unmodifiableList(this.outputs);
    }

    @Override @Nullable
    public IBlueprintPin getInput(String id) {
        Objects.requireNonNull(id, "Pin ID may not be null");

        return this.inputs.stream()
            .filter(pin -> pin.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override @Nullable
    public IBlueprintPin getOutput(String id) {
        Objects.requireNonNull(id, "Pin ID may not be null");

        return this.outputs.stream()
            .filter(pin -> pin.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public abstract void validate();

    @Override
    public abstract void execute(IExecutionContext context);

    @Override
    public NodePosition getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(NodePosition position) {
        Objects.requireNonNull(position, "Position may not be null");
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof BlueprintNode other)) {
            return false;
        }

        // Nodes are equal if they have the same ID
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}
