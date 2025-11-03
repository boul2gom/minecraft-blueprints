package fr.boul2gom.blueprints.api.exception;

/**
 * Represents a base exception in the blueprint system.
 *
 * This exception serves as a general-purpose runtime exception for errors occurring
 * within the blueprint framework. It is designed to be extended by more specific
 * exceptions to provide detailed and contextual error information.
 *
 * Subclasses of {@code BlueprintException} may include:
 * - {@code ValidationException} for validation-related issues.
 * - {@code ConnectionException} for connection-related errors in the blueprint graph.
 * - {@code NodeNotFoundException} for cases where a specific node cannot be located.
 * - {@code CycleDetectedException} for graph validation issues, such as cyclic dependencies.
 *
 * By using this base class, the blueprint framework ensures a consistent hierarchy of
 * exception types for error handling and diagnostics.
 */
public class BlueprintException extends RuntimeException {

    /**
     * Constructs a new {@code BlueprintException} with the specified detail message.
     * @param message the detail message.
     */
    public BlueprintException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code BlueprintException} with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public BlueprintException(String message, Throwable cause) {
        super(message, cause);
    }
}
