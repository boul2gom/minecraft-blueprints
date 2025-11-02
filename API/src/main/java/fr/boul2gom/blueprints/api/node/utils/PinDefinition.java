package fr.boul2gom.blueprints.api.node.utils;

import fr.boul2gom.blueprints.api.pin.PinType;

public record PinDefinition(String id, String name, PinType type) {

    public PinDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Pin ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pin name cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Pin type cannot be null");
        }
    }
}
