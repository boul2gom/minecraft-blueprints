package fr.boul2gom.blueprints.api.execution;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The IExecutionResult interface represents the outcome of executing a blueprint or process.
 * It provides methods to query the result state, execution metadata, and error details.
 */
public interface IExecutionResult {

    /**
     * Returns true if the execution completed successfully.
     * @return true if the execution completed successfully, false otherwise
     */
    boolean isSuccess();

    /**
     * Returns the error message if the execution failed.
     * @return the error message, or null if no error occurred
     */
    @Nullable
    String error();

    /**
     * Returns the execution time.
     * @return the execution time as a Duration
     */
    Duration execution_time();

    /**
     * Returns the number of nodes executed.
     * @return the number of nodes executed
     */
    int nodes_executed();

    /**
     * Returns the execution result type.
     * @return the execution result type
     */
    Type type();

    /**
     * The Type enum defines possible outcomes of a blueprint execution process.
     * It categorizes the result into one of several predefined statuses to indicate
     * the state or end condition of the execution.
     */
    enum Type {
        /**
         * Represents a successful outcome of a blueprint execution process.
         */
        SUCCESS,
        /**
         * Represents a failed outcome of a blueprint execution process.
         */
        ERROR,
        /**
         * Represents a timeout outcome of a blueprint execution process.
         */
        TIMEOUT,
        /**
         * Represents a node limit exceeded outcome of a blueprint execution process.
         */
        NODE_LIMIT_EXCEEDED,
        /**
         * Represents a validation failure outcome of a blueprint execution process.
         */
        VALIDATION_FAILED
    }
}
