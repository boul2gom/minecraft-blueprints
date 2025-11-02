package fr.boul2gom.blueprints.api.connection;

import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

public interface IBlueprintConnection {

    String getId();

    IBlueprintPin getInput();
    IBlueprintPin getOutput();
    IBlueprintPin getOther(IBlueprintPin pin);

    boolean isValid();
}
