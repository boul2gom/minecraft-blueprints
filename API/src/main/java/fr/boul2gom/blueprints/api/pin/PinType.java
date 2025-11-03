package fr.boul2gom.blueprints.api.pin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Quaterniond;
import org.joml.Vector3f;

/**
 * The PinType enum defines various types of pins used within a blueprint system.
 * Pins represent connection points where data or execution flow interacts in a blueprint.
 *
 * Each PinType provides the following properties:
 * - A readable name for the pin type.
 * - The associated Java class type that the pin type represents.
 * - A color typically used for visualization within the system.
 *
 * This enum allows blueprint systems to categorize and validate connections between pins
 * based on their types. For example, it ensures that pins of incompatible types cannot
 * be connected.
 */
public enum PinType {

    /** Represents an execution flow pin, used to control the flow of execution between nodes. */
    EXECUTION_FLOW("Execution", Void.class, 0xFF00FF00),
    /** Represents a boolean pin, used to represent a true or false value. */
    BOOLEAN("Boolean", Boolean.class, 0xFFFF0000),
    /** Represents an integer pin, used to represent an integer value. */
    INTEGER("Integer", Integer.class, 0xFF0000FF),
    /** Represents a floating point pin, used to represent a floating point value. */
    FLOAT("Float", Float.class, 0xFFFFFF00),
    /** Represents a string pin, used to represent a string value. */
    STRING("String", String.class, 0xFFFF00FF),

    /** Represents a vector pin, used to represent a 3D vector. */
    VECTOR("Vector 3D", Vector3f.class, 0xFF00FFFF),
    /** Represents a rotator pin, used to represent a rotation in 3D space. */
    ROTATOR("Rotator", Quaterniond.class, 0xFFFF8000),

    /** Represents an entity pin, used to represent a Minecraft entity */
    ENTITY("Entity", Entity.class, 0xFFFF4080),
    /** Represents a block state pin, used to represent a Minecraft block state */
    BLOCK_STATE("Block State", BlockState.class, 0xFF4080FF),
    /** Represents a block position pin, used to represent a Minecraft block position */
    BLOCK_POS("Block Position", BlockPos.class, 0xFF80FF40),
    /** Represents an item pin, used to represent a Minecraft item stack */
    ITEM("Item", ItemStack.class, 0xFFFF8040),
    /** Represents a world pin, used to represent a Minecraft world */
    WORLD("World", World.class, 0xFF808080);

    /** The name of the pin type. */
    private final String name;
    /** The Java class type that the pin type represents. */
    private final Class<?> clazz;
    /** The color typically used for visualization within the system. */
    private final int color;

    /**
     * Constructs a new PinType with the specified name, class, and color.
     * @param name the name representing the type of the pin.
     * @param clazz the Java class type that the pin type represents.
     * @param color the color typically used for visualization within the system.
     */
    PinType(String name, Class<?> clazz, int color) {
        this.name = name;
        this.clazz = clazz;
        this.color = color;
    }

    /**
     * Gets the name of the pin type.
     * @return the name of the pin type.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Java class type that the pin type represents.
     * @return the Java class type that the pin type represents.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Gets the color typically used for visualization within the system.
     * @return the color typically used for visualization within the system.
     */
    public int getColor() {
        return color;
    }

    /**
     * Checks if the pin type is an execution flow type.
     * @return true if the pin type is an execution flow type, false otherwise.
     */
    public boolean isExecution() {
        return this == EXECUTION_FLOW;
    }
}
