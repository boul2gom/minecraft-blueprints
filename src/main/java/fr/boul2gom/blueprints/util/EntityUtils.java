package fr.boul2gom.blueprints.util;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Utility class for common entity operations to reduce code duplication.
 */
public final class EntityUtils {

    private EntityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get the display name of an entity, falling back to the entity's name if display name is null.
     *
     * @param entity The entity to get the display name from
     * @return The display name or name of the entity
     */
    public static Text getDisplayName(Entity entity) {
        Objects.requireNonNull(entity, "Entity may not be null");
        
        final Text displayName = entity.getDisplayName();
        return displayName != null ? displayName : entity.getName();
    }
}
