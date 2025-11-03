package fr.boul2gom.blueprints.api.execution.context;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * The IVariableRegistry interface provides a dedicated system for managing runtime variables
 * during blueprint execution. This registry supports typed variable access, existence checks,
 * and snapshot capabilities for debugging and state inspection.
 *
 * Primary Responsibilities:
 * - Store and retrieve runtime variables with type-safe access
 * - Check for variable existence before retrieval
 * - Provide immutable snapshots of the current variable state
 * - Support variable removal and clearing
 */
public interface IVariableRegistry {

    /**
     * Retrieves the value of a variable by its key.
     *
     * @param key the variable key
     * @return the variable value, or null if not found
     */
    @Nullable
    Object get(final String key);

    /**
     * Retrieves the value of a variable with type casting.
     *
     * @param key the variable key
     * @param type the expected type
     * @param <T> the type parameter
     * @return an Optional containing the typed value, or empty if not found or wrong type
     */
    <T> Optional<T> get(final String key, final Class<T> type);

    /**
     * Sets the value of a variable.
     *
     * @param key the variable key
     * @param value the variable value
     */
    void set(final String key, final Object value);

    /**
     * Checks if a variable exists in the registry.
     *
     * @param key the variable key
     * @return true if the variable exists, false otherwise
     */
    boolean has(final String key);

    /**
     * Removes a variable from the registry.
     *
     * @param key the variable key
     * @return the removed value, or null if not found
     */
    @Nullable
    Object remove(final String key);

    /**
     * Clears all variables from the registry.
     */
    void clear();

    /**
     * Returns an immutable snapshot of all variables.
     * Modifications to the returned map will not affect the registry.
     *
     * @return an immutable copy of all variables
     */
    Map<String, Object> snapshot();

    /**
     * Returns the number of variables currently stored.
     *
     * @return the variable count
     */
    int size();

    /**
     * Checks if the registry is empty.
     *
     * @return true if no variables are stored, false otherwise
     */
    boolean isEmpty();
}