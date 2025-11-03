package fr.boul2gom.blueprints.api.pin;

import fr.boul2gom.blueprints.api.connection.IBlueprintConnection;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;

import java.util.Set;

/**
 * The IBlueprintPin interface represents a pin within a blueprint system.
 * Pins are connection points for nodes, facilitating data or execution flow
 * between nodes within the blueprint.
 *
 * Responsibilities of a blueprint pin include:
 * - Identifying itself uniquely and providing readable metadata such as a string name.
 * - Specifying its type and direction (input or output) within the blueprint.
 * - Associating itself with a parent node to indicate its context.
 * - Managing its connection state and relationships with other pins via connections.
 * - Enforcing rules for establishing valid connections to other pins based on type and direction.
 */
public interface IBlueprintPin {

    /**
     * Get the unique identifier of the pin.
     * @return unique identifier of the pin.
     */
    String getId();

    /**
     * Get the name of the pin.
     * @return name of the pin.
     */
    String getName();

    /**
     * Get the display name of the pin.
     * @return the display name of the pin.
     */
    String getDisplay();

    /**
     * Get the type of the pin.
     * @return type of the pin.
     */
    PinType getType();

    /**
     * Check if the pin is an execution flow pin.
     * @return true if the pin is an execution pin, false otherwise.
     */
    boolean isExecution();

    /**
     * Get the direction of the pin.
     * @return direction of the pin.
     */
    PinDirection getDirection();

    /**
     * Get the parent node of the pin.
     * @return parent node of the pin.
     */
    IBlueprintNode getNode();

    /**
     * Check if the pin is connected to another pin.
     * @return true if the pin is connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Check if the pin is connected to another pin.
     * @param pin the pin to check connection to.
     * @return true if the pin is connected to the specified pin, false otherwise.
     */
    boolean isConnectedTo(IBlueprintPin pin);

    /**
     * Check if the pin can connect to another pin.
     * @param pin the pin to check connection to.
     * @return true if the pin can connect to the specified pin, false otherwise.
     */
    boolean can_connect_to(IBlueprintPin pin);

    /**
     * Connect the pin to another pin.
     * @param pin the pin to connect to.
     */
    void connect_to(IBlueprintPin pin);

    /**
     * Disconnect the pin from any other pin.
     */
    void disconnect();

    /**
     * Disconnect the pin from another pin.
     * @param pin the pin to disconnect from.
     */
    void disconnect_from(IBlueprintPin pin);

    /**
     * Get all connections associated with the pin.
     * @return a set of connections.
     */
    Set<IBlueprintConnection> getConnections();

    /**
     * Get all pins associated with the pin.
     * @return a set of pins.
     */
    Set<IBlueprintPin> getPins();
}
