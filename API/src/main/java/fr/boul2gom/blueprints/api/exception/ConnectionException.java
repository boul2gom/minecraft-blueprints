package fr.boul2gom.blueprints.api.exception;

/**
 * Exception thrown when an invalid connection is attempted in a blueprint graph.
 * This exception is a specialized form of {@link BlueprintException}, providing details
 * about why a connection-related operation failed.
 *
 * This may be used in scenarios such as:
 * - Attempting to connect incompatible pins.
 * - Violations of connection rules defined in the blueprint system.
 * - General issues preventing a valid connection.
 */
public class ConnectionException extends BlueprintException {

    /**
     * Constructs a new {@code ConnectionException} with the specified detail message.
     * @param message the detail message.
     */
    public ConnectionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ConnectionException} with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
