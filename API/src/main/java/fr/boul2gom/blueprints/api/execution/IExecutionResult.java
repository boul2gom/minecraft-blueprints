package fr.boul2gom.blueprints.api.execution;

import org.jetbrains.annotations.Nullable;

public interface IExecutionResult {

    // Check if execution was successful
    boolean isSuccess();

    // Get error message if execution failed
    @Nullable
    String getError();

    // Get execution time in milliseconds
    long getExecutionTime();

    // Get number of nodes executed
    int getNodesExecuted();

    // Get execution result type
    ExecutionResultType getType();

    enum ExecutionResultType {
        SUCCESS,           // Execution completed successfully
        ERROR,             // Execution failed with error
        TIMEOUT,           // Execution exceeded time limit
        NODE_LIMIT_EXCEEDED, // Execution exceeded node limit
        VALIDATION_FAILED  // Graph validation failed
    }
}
