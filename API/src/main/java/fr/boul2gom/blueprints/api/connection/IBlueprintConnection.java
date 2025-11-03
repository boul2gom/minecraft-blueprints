package fr.boul2gom.blueprints.api.connection;

import fr.boul2gom.blueprints.api.pin.IBlueprintPin;

/**
 * Represents a connection in a blueprint system, linking two pins together.
 * A connection is defined by a unique identifier, an input pin, and an output pin.
 * It allows for querying its validity and determining the counterpart pin in the connection.
 */
public interface IBlueprintConnection {

    /**
     * Get the unique identifier of the connection.
     * @return unique identifier of the connection.
     */
    String getId();

    /**
     * Get the input pin of the connection.
     * @return input pin of the connection.
     */
    IBlueprintPin getInput();

    /**
     * Get the output pin of the connection.
     * @return output pin of the connection.
     */
    IBlueprintPin getOutput();

    /**
     * Get the counterpart pin of the connection.
     * @param pin the pin to get the counterpart of.
     * @return the counterpart pin of the connection.
     */
    IBlueprintPin getOther(IBlueprintPin pin);

    /**
     * Check if the connection is valid.
     * @return true if the connection is valid, false otherwise.
     */
    boolean isValid();
}
