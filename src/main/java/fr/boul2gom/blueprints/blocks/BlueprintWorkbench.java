package fr.boul2gom.blueprints.blocks;

import fr.boul2gom.blueprints.blocks.voxels.WorkbenchVoxels;
import fr.boul2gom.blueprints.screens.BlueprintScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static fr.boul2gom.blueprints.MinecraftBlueprints.LOGGER;

public class BlueprintWorkbench extends Block {

    public static final String ID = "blueprint_workbench";
    public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);

    private static final Map<Pair, VoxelShape> COMBINED_SHAPES = new HashMap<>();

    private static final VoxelShape LEFT_SHAPE_NORTH = WorkbenchVoxels.create_left_shape();
    private static final VoxelShape LEFT_SHAPE_SOUTH = rotate_shape(LEFT_SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape LEFT_SHAPE_EAST = rotate_shape(LEFT_SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape LEFT_SHAPE_WEST = rotate_shape(LEFT_SHAPE_NORTH, Direction.WEST);

    private static final VoxelShape RIGHT_SHAPE_NORTH = WorkbenchVoxels.create_right_shape();
    private static final VoxelShape RIGHT_SHAPE_SOUTH = rotate_shape(RIGHT_SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape RIGHT_SHAPE_EAST = rotate_shape(RIGHT_SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape RIGHT_SHAPE_WEST = rotate_shape(RIGHT_SHAPE_NORTH, Direction.WEST);

    private static final VoxelShape TOOLBOX_SHAPE_NORTH = WorkbenchVoxels.create_toolbox_shape();
    private static final VoxelShape TOOLBOX_SHAPE_SOUTH = rotate_shape(TOOLBOX_SHAPE_NORTH, Direction.SOUTH);
    private static final VoxelShape TOOLBOX_SHAPE_EAST = rotate_shape(TOOLBOX_SHAPE_NORTH, Direction.EAST);
    private static final VoxelShape TOOLBOX_SHAPE_WEST = rotate_shape(TOOLBOX_SHAPE_NORTH, Direction.WEST);

    public BlueprintWorkbench(Settings settings) {
        super(settings);

        final BlockState default_state = this.getStateManager().getDefaultState();
        final BlockState state = default_state.with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(TYPE, Type.LEFT);
        this.setDefaultState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING, TYPE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        final World world = ctx.getWorld();
        final BlockPos pos = ctx.getBlockPos();
        final Direction direction = ctx.getHorizontalPlayerFacing();
        final Direction side_direction = this.rotate(direction);

        boolean can_place_right = world.getBlockState(pos.offset(side_direction)).canReplace(ctx);
        boolean can_place_toolbox = world.getBlockState(pos.offset(side_direction).up()).canReplace(ctx);

        if (can_place_right && can_place_toolbox) {
            return this.getDefaultState().with(Properties.HORIZONTAL_FACING, direction);
        }

        return null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack item) {
        super.onPlaced(world, pos, state, placer, item);

        if (state.get(TYPE) == Type.LEFT) {
            final Direction direction = state.get(Properties.HORIZONTAL_FACING);
            final Direction side_direction = this.rotate(direction);

            world.setBlockState(pos.offset(side_direction), state.with(TYPE, Type.RIGHT));
            world.setBlockState(pos.offset(side_direction).up(), state.with(TYPE, Type.TOOLBOX));
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        final Direction direction = state.get(Properties.HORIZONTAL_FACING);
        final Direction side_direction = this.rotate(direction);

        switch (state.get(TYPE)) {
            case LEFT:
                world.setBlockState(pos.offset(side_direction), Blocks.AIR.getDefaultState());
                world.setBlockState(pos.offset(side_direction).up(), Blocks.AIR.getDefaultState());
                break;
            case RIGHT:
                world.setBlockState(pos.up(), Blocks.AIR.getDefaultState());
                world.setBlockState(pos.offset(side_direction.getOpposite()), Blocks.AIR.getDefaultState());
                break;
            case TOOLBOX:
                world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
                world.setBlockState(pos.down().offset(side_direction.getOpposite()), Blocks.AIR.getDefaultState());
                break;
        }

        super.onStateReplaced(state, world, pos, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Type type = state.get(TYPE) != null ? state.get(TYPE) : Type.LEFT;
        Direction direction = state.get(Properties.HORIZONTAL_FACING) != null ? state.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;
        Pair pair = new Pair(type, direction);

        return COMBINED_SHAPES.computeIfAbsent(pair, k -> {
            final Direction side_direction = this.rotate(direction);
            VoxelShape shape = shape_for_state(state);

            shape = switch (type) {
                case LEFT -> {
                    VoxelShape right_shape = shape_for_state(state.with(TYPE, Type.RIGHT));
                    VoxelShape toolbox_shape = shape_for_state(state.with(TYPE, Type.TOOLBOX));

                    VoxelShape combined = VoxelShapes.union(shape, translate_shape(right_shape, side_direction));
                    yield VoxelShapes.union(combined, translate_shape(translate_shape(toolbox_shape, side_direction), Direction.UP));
                }
                case RIGHT -> {
                    VoxelShape toolbox_shape = shape_for_state(state.with(TYPE, Type.TOOLBOX));
                    VoxelShape left_shape = shape_for_state(state.with(TYPE, Type.LEFT));

                    VoxelShape combined = VoxelShapes.union(shape, translate_shape(toolbox_shape, Direction.UP));
                    yield VoxelShapes.union(combined, translate_shape(left_shape, side_direction.getOpposite()));
                }
                case TOOLBOX -> {
                    VoxelShape right_shape = shape_for_state(state.with(TYPE, Type.RIGHT));
                    VoxelShape left_shape = shape_for_state(state.with(TYPE, Type.LEFT));

                    VoxelShape combined = VoxelShapes.union(shape, translate_shape(right_shape, Direction.DOWN));
                    yield VoxelShapes.union(combined, translate_shape(translate_shape(left_shape, Direction.DOWN), side_direction.getOpposite()));
                }
            };
            return shape;
        });
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        LOGGER.info("Opening Blueprint Workbench GUI...");

        if (!world.isClient()) {
            LOGGER.info("Server-side detected, opening screen handler.");
            player.openHandledScreen(create_factory(world, pos));
        }

        return ActionResult.SUCCESS;
    }

    private NamedScreenHandlerFactory create_factory(World world, BlockPos pos) {
        final Text title = Text.literal("Blueprint Workbench");

        return new SimpleNamedScreenHandlerFactory(BlueprintScreenHandler.FACTORY, title);
    }

    private Direction rotate(Direction dir) {
        return switch (dir) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> dir;
        };
    }

    private static VoxelShape shape_for_state(BlockState state) {
        Type type = state.get(TYPE) != null ? state.get(TYPE) : Type.LEFT;
        Direction facing = state.get(Properties.HORIZONTAL_FACING) != null ? state.get(Properties.HORIZONTAL_FACING) : Direction.NORTH;

        return switch (type) {
            case LEFT -> switch (facing) {
                case NORTH -> LEFT_SHAPE_NORTH;
                case SOUTH -> LEFT_SHAPE_SOUTH;
                case EAST -> LEFT_SHAPE_EAST;
                case WEST -> LEFT_SHAPE_WEST;
                default -> VoxelShapes.empty();
            };
            case RIGHT -> switch (facing) {
                case NORTH -> RIGHT_SHAPE_NORTH;
                case SOUTH -> RIGHT_SHAPE_SOUTH;
                case EAST -> RIGHT_SHAPE_EAST;
                case WEST -> RIGHT_SHAPE_WEST;
                default -> VoxelShapes.empty();
            };
            case TOOLBOX -> switch (facing) {
                case NORTH -> TOOLBOX_SHAPE_NORTH;
                case SOUTH -> TOOLBOX_SHAPE_SOUTH;
                case EAST -> TOOLBOX_SHAPE_EAST;
                case WEST -> TOOLBOX_SHAPE_WEST;
                default -> VoxelShapes.empty();
            };
        };
    }

    private static VoxelShape rotate_shape(VoxelShape shape, Direction to) {
        final int from_horizontal = Direction.NORTH.getHorizontalQuarterTurns();
        final int to_horizontal = to.getHorizontalQuarterTurns();

        int times = (to_horizontal - from_horizontal + 4) % 4;
        AtomicReference<VoxelShape> result = new AtomicReference<>(shape);

        for (int i = 0; i < times; i++) {
            final VoxelShape current = result.get();
            result.set(VoxelShapes.empty());
            current.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                VoxelShape rotated = VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX);
                result.set(VoxelShapes.union(result.get(), rotated));
            });
        }
        return result.get();
    }

    private static VoxelShape translate_shape(VoxelShape shape, Direction direction) {
        return translate_shape(shape, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
    }

    private static VoxelShape translate_shape(VoxelShape shape, double x, double y, double z) {
        AtomicReference<VoxelShape> result = new AtomicReference<>(VoxelShapes.empty());
        shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            VoxelShape translated = VoxelShapes.cuboid(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z);
            result.set(VoxelShapes.union(result.get(), translated));
        });
        return result.get();
    }

    public enum Type implements StringIdentifiable {
        LEFT("left"),
        RIGHT("right"),
        TOOLBOX("toolbox");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return this.name;
        }
    }

    private record Pair(Type type, Direction direction) {

    }
}