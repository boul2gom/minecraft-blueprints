package fr.boul2gom.blueprints.api.pin;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.Set;

public interface IBlueprintPin {

    String getId();
    String getName();

    PinType getType();
    PinDirection getDirection();

    IBlueprintNode getNode();

    boolean isConnected();
    boolean isConnectedTo(IBlueprintPin pin);

    boolean canConnectTo(IBlueprintPin pin);
    void connectTo(IBlueprintPin pin);

    void disconnect();
    void disconnectFrom(IBlueprintPin pin);

    Set<IBlueprintConnection> getConnections();
    Set<IBlueprintPin> getPins();

    String getDisplay();
}
