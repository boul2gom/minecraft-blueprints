package fr.boul2gom.blueprints.api.connection;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import fr.boul2gom.blueprints.api.pin.PinDirection;

import java.util.Objects;

public class BlueprintConnection implements IBlueprintConnection {

    private final String id;
    private final IBlueprintPin input;
    private final IBlueprintPin output;

    public BlueprintConnection(String id, IBlueprintPin input, IBlueprintPin output) {
        Objects.requireNonNull(id, "Connection id may not be null");
        Objects.requireNonNull(input, "Input pin may not be null");
        Objects.requireNonNull(output, "Output pin may not be null");

        if (input.getDirection() != PinDirection.INPUT) {
            throw new IllegalArgumentException("Input pin must be of direction INPUT");
        }

        if (output.getDirection() != PinDirection.OUTPUT) {
            throw new IllegalArgumentException("Output pin must be of direction OUTPUT");
        }

        this.id = id;
        this.input = input;
        this.output = output;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public IBlueprintPin getInput() {
        return this.input;
    }

    @Override
    public IBlueprintPin getOutput() {
        return this.output;
    }

    @Override
    public IBlueprintPin getOther(IBlueprintPin pin) {
        return pin.equals(this.input) ? this.output : this.input;
    }

    @Override
    public boolean isValid() {
        return this.output.can_connect_to(this.input);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlueprintConnection other)) return false;

        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return String.format("BlueprintConnection(id=%s, output=%s, input=%s)", this.id, this.output.getId(), this.input.getId());
    }
}
