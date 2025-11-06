package fr.boul2gom.blueprints.execution.context;

import fr.boul2gom.blueprints.api.execution.context.IVariableRegistry;
import fr.boul2gom.blueprints.api.execution.context.IVariableProvider;

import java.util.Map;
import java.util.Optional;

/**
 * Provider implementation for managing blueprint variables.
 * Wraps IVariableRegistry to provide a simpler API.
 */
public record VariableProvider(IVariableRegistry registry) implements IVariableProvider {

    @Override
    public void set(String name, Object value) {
        this.registry.set(name, value);
    }

    @Override
    public Optional<Object> get(String name) {
        return Optional.ofNullable(this.registry.get(name));
    }

    @Override
    public boolean has(String name) {
        return this.registry.has(name);
    }

    @Override
    public void remove(String name) {
        this.registry.remove(name);
    }

    @Override
    public Map<String, Object> getAll() {
        return this.registry.snapshot();
    }

    @Override
    public void clear() {
        this.registry.clear();
    }
}
