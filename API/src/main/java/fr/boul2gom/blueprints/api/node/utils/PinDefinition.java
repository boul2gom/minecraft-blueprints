package fr.boul2gom.blueprints.api.node.utils;

import fr.boul2gom.blueprints.api.pin.PinType;

import java.util.Objects;

/**
 * Represents a pin definition for use in a node configuration.
 * A pin definition includes an identifier, a name, and a type.
 * Pins are used to define the inputs and outputs of a node.
 *
 * @param id   the unique identifier of the pin (must not be null or blank)
 * @param name the name of the pin (must not be null or blank)
 * @param type the type of the pin (must not be null)
 */
public record PinDefinition(String id, String name, PinType type) {

    /**
     * Constructs a new instance of {@code PinDefinition}.
     * This constructor enforces that the pin ID, name, and type are not null,
     * and that the ID and name are not blank.
     *
     * @param id   the unique identifier of the pin, which must not be null or blank
     * @param name the name of the pin, which must not be null or blank
     * @param type the type of the pin, which must not be null
     * @throws NullPointerException     if {@code id}, {@code name}, or {@code type} is null
     * @throws IllegalArgumentException if {@code id} or {@code name} is blank, or if {@code id} has invalid format
     */
    public PinDefinition {
        Objects.requireNonNull(id, "Pin ID may not be null");
        Objects.requireNonNull(name, "Pin name may not be null");
        Objects.requireNonNull(type, "Pin type may not be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Pin ID cannot be blank");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Pin name cannot be blank");
        }

        // Validate ID format: must match [a-z][a-z0-9_]*
        if (!id.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException(
                String.format("Pin ID must match pattern [a-z][a-z0-9_]* (lowercase letters, numbers, underscores, must start with letter): '%s'", id)
            );
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PinDefinition other)) return false;

        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
