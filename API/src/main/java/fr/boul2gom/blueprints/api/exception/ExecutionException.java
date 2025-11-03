package fr.boul2gom.blueprints.api.exception;

// Exception thrown during blueprint execution
public class ExecutionException extends BlueprintException {

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
