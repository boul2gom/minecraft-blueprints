package fr.boul2gom.blueprints.api.pin;

/**
 * The PinDirection enum represents the direction of a pin within the blueprint system.
 * It is used to define whether a pin is an input or output.
 *
 * This enum is typically used in the context of blueprint pins to specify how
 * data or execution flow is expected to interact with the pin.
 */
public enum PinDirection {

    /** Represents an input pin. */
    INPUT("Input"),
    /** Represents an output pin. */
    OUTPUT("Output");

    /** The name of the pin direction. */
    private final String name;

    /**
     * Constructs a new PinDirection with the specified name.
     *
     * @param name the name representing the direction of the pin.
     */
    PinDirection(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the pin direction.
     * @return the name of the pin direction.
     */
    public String getName() {
        return name;
    }
}
