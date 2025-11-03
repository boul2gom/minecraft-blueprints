package fr.boul2gom.blueprints.api.exception.execution;

import fr.boul2gom.blueprints.api.exception.BlueprintException;

/**
 * Represents an exception that occurs during the execution of a process.
 * This exception is a subclass of {@code BlueprintException} and serves as
 * a base class for specific execution-related exceptions.
 */
public class ExecutionException extends BlueprintException {

    /**
     * Constructs a new {@code ExecutionException} with the specified detail message.
     * @param message the detail message.
     */
    public ExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ExecutionException} with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
