package fr.boul2gom.blueprints.api.execution.context;

import java.util.Map;
import java.util.Optional;

/**
 * Provider for managing blueprint execution variables.
 * Handles variable storage, retrieval, and scope management.
 */
public interface IVariableProvider {

    /**
     * Sets a variable in the current scope.
     *
     * @param name The variable name
     * @param value The variable value
     */
    void set(String name, Object value);

    /**
     * Gets a variable from the current scope.
     *
     * @param name The variable name
     * @return Optional containing the value if found
     */
    Optional<Object> get(String name);

    /**
     * Checks if a variable exists in the current scope.
     *
     * @param name The variable name
     * @return true if the variable exists
     */
    boolean has(String name);

    /**
     * Removes a variable from the current scope.
     *
     * @param name The variable name
     */
    void remove(String name);

    /**
     * Gets all variables as an immutable map.
     *
     * @return Map of all variables
     */
    Map<String, Object> getAll();

    /**
     * Clears all variables in the current scope.
     */
    void clear();
}
