package fr.boul2gom.blueprints.api.exception.validation;

import fr.boul2gom.blueprints.api.exception.BlueprintException;

/**
 * Exception indicating a validation failure in a blueprint processing context.
 *
 * This exception serves as a base for more specific validation-related exceptions
 * and can be used to signal issues found during the validation phase, such as
 * logical inconsistencies, invalid configurations, or other domain-specific errors.
 */
public class ValidationException extends BlueprintException {

    /**
     * Constructs a new {@code ValidationException} with the specified detail message.
     * @param message the detail message.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ValidationException} with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
