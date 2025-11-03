package fr.boul2gom.blueprints.api.exception;

// Exception thrown when an invalid connection is attempted
public class ConnectionException extends BlueprintException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
