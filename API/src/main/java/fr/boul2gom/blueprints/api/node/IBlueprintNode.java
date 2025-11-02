package fr.boul2gom.blueprints.api.node;

import fr.boul2gom.blueprints.api.pin.IBlueprintPin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IBlueprintNode {

    String getId();
    String getName();

    List<? extends IBlueprintPin> getInputs();
    List<? extends IBlueprintPin> getOutputs();

    @Nullable IBlueprintPin getInput(String id);
    @Nullable IBlueprintPin getOutput(String id);

    void validate();
    void execute();

    NodePosition getPosition();
    void setPosition(NodePosition position);
}
