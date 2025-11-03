package fr.boul2gom.blueprints.api.exception;

// Base exception for all blueprint-related errors
public class BlueprintException extends RuntimeException {

    public BlueprintException(String message) {
        super(message);
    }

    public BlueprintException(String message, Throwable cause) {
        super(message, cause);
    }
}
