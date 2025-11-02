package fr.boul2gom.blueprints.api.pin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Quaterniond;
import org.joml.Vector3f;

public enum PinType {

    EXECUTION_FLOW("Execution", Void.class, 0xFF00FF00),
    BOOLEAN("Boolean", Boolean.class, 0xFFFF0000),
    INTEGER("Integer", Integer.class, 0xFF0000FF),
    FLOAT("Float", Float.class, 0xFFFFFF00),
    STRING("String", String.class, 0xFFFF00FF),

    VECTOR("Vector 3D", Vector3f.class, 0xFF00FFFF),
    ROTATOR("Rotator", Quaterniond.class, 0xFFFF8000),

    ENTITY("Entity", Entity.class, 0xFFFF4080),
    BLOCK_STATE("Block State", BlockState.class, 0xFF4080FF),
    BLOCK_POS("Block Position", BlockPos.class, 0xFF80FF40),
    ITEM("Item", ItemStack.class, 0xFFFF8040),
    WORLD_REF("World", World.class, 0xFF808080);

    private final String name;
    private final Class<?> clazz;

    private final int color;

    PinType(String name, Class<?> clazz, int color) {
        this.name = name;
        this.clazz = clazz;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public int getColor() {
        return color;
    }

    public boolean isExecution() {
        return this == EXECUTION_FLOW;
    }
}
