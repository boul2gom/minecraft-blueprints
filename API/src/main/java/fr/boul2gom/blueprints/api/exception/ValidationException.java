package fr.boul2gom.blueprints.api.exception;

// Exception thrown when graph validation fails
public class ValidationException extends BlueprintException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
