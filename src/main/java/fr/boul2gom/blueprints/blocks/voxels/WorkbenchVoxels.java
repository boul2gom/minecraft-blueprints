package fr.boul2gom.blueprints.blocks.voxels;

import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class WorkbenchVoxels {

    public static VoxelShape create_left_shape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 1.0, 0.0625, 0.9375, 1.00625, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0125, 0.40625, 0.4375, 1.0, 0.53125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.0, 0.0, 0.08125, 0.1875, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.0, 0.8125, 0.08125, 0.1875, 1.0));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.0625, 0.0625, 0.08125, 0.25, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.125, 0.125, 0.08125, 0.3125, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.1875, 0.1875, 0.08125, 0.375, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.25, 0.25, 0.08125, 0.4375, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.0625, 0.75, 0.08125, 0.25, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.125, 0.6875, 0.08125, 0.3125, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.1875, 0.625, 0.08125, 0.375, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.25, 0.5625, 0.08125, 0.4375, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.3125, 0.5, 0.08125, 0.5, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.3125, 0.3125, 0.08125, 0.5, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.5, 0.5, 0.08125, 0.6875, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.5625, 0.5625, 0.08125, 0.75, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.625, 0.625, 0.08125, 0.8125, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.6875, 0.6875, 0.08125, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.75, 0.75, 0.08125, 0.9375, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.5, 0.3125, 0.08125, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.5625, 0.25, 0.08125, 0.75, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.625, 0.1875, 0.08125, 0.8125, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.6875, 0.125, 0.08125, 0.875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.01875, 0.75, 0.0625, 0.08125, 0.9375, 0.25));
        return shape;
    }

    public static VoxelShape create_right_shape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0, 0.8125, 0.0, 1.0, 1.0, 1.0));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0, 0.40625, 0.4375, 0.9875, 0.53125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.0, 0.0, 0.98125, 0.1875, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.0, 0.8125, 0.98125, 0.1875, 1.0));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.0625, 0.75, 0.98125, 0.25, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.125, 0.6875, 0.98125, 0.3125, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.1875, 0.625, 0.98125, 0.375, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.25, 0.5625, 0.98125, 0.4375, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.3125, 0.5, 0.98125, 0.5, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.5, 0.3125, 0.98125, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.5625, 0.25, 0.98125, 0.75, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.625, 0.1875, 0.98125, 0.8125, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.6875, 0.125, 0.98125, 0.875, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.75, 0.0625, 0.98125, 0.9375, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.0625, 0.0625, 0.98125, 0.25, 0.25));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.125, 0.125, 0.98125, 0.3125, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.1875, 0.1875, 0.98125, 0.375, 0.375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.25, 0.25, 0.98125, 0.4375, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.3125, 0.3125, 0.98125, 0.5, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.5, 0.5, 0.98125, 0.6875, 0.6875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.5625, 0.5625, 0.98125, 0.75, 0.75));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.625, 0.625, 0.98125, 0.8125, 0.8125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.6875, 0.6875, 0.98125, 0.875, 0.875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.91875, 0.75, 0.75, 0.98125, 0.9375, 0.9375));
        return shape;
    }

    public static VoxelShape create_toolbox_shape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.125, 0.0, 0.0625, 0.875, 0.375, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.375, 0.1875, 0.375, 0.4375, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.4375, 0.1875, 0.6875, 0.5, 0.3125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.625, 0.375, 0.1875, 0.6875, 0.4375, 0.3125));
        return shape;
    }
}
