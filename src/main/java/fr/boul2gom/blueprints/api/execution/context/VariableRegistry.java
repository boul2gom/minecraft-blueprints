package fr.boul2gom.blueprints.api.execution.context;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class VariableRegistry implements IVariableRegistry {

    private final Map<String, Object> variables;

    public VariableRegistry() {
        this.variables = new HashMap<>();
    }

    @Override
    @Nullable
    public Object get(String key) {
        Objects.requireNonNull(key, "Variable key cannot be null");
        return this.variables.get(key);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Objects.requireNonNull(key, "Variable key cannot be null");
        Objects.requireNonNull(type, "Type cannot be null");

        final Object value = this.variables.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }

        return Optional.empty();
    }

    @Override
    public void set(String key, Object value) {
        Objects.requireNonNull(key, "Variable key cannot be null");
        this.variables.put(key, value);
    }

    @Override
    public boolean has(String key) {
        Objects.requireNonNull(key, "Variable key cannot be null");
        return this.variables.containsKey(key);
    }

    @Override
    @Nullable
    public Object remove(String key) {
        Objects.requireNonNull(key, "Variable key cannot be null");
        return this.variables.remove(key);
    }

    @Override
    public void clear() {
        this.variables.clear();
    }

    @Override
    public Map<String, Object> snapshot() {
        return Map.copyOf(this.variables);
    }

    @Override
    public int size() {
        return this.variables.size();
    }

    @Override
    public boolean isEmpty() {
        return this.variables.isEmpty();
    }

    @Override
    public String toString() {
        return MinecraftBlueprints.GSON.toJson(this);
    }
}