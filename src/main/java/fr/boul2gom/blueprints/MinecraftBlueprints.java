package fr.boul2gom.blueprints;

import com.google.gson.Gson;
import fr.boul2gom.blueprints.api.execution.context.ExecutionContext;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.blocks.BlueprintWorkbench;
import fr.boul2gom.blueprints.execution.BlueprintExecutor;
import fr.boul2gom.blueprints.execution.BlueprintScheduler;
import fr.boul2gom.blueprints.nodes.*;
import fr.boul2gom.blueprints.nodes.action.DelayNode;
import fr.boul2gom.blueprints.nodes.action.PrintNode;
import fr.boul2gom.blueprints.nodes.event.*;
import fr.boul2gom.blueprints.nodes.flow.*;
import fr.boul2gom.blueprints.nodes.math.*;
import fr.boul2gom.blueprints.nodes.variable.*;
import fr.boul2gom.blueprints.screens.BlueprintScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MinecraftBlueprints implements ModInitializer {

	public static final String MOD_ID = "minecraft-blueprints";
    public static final String MOD_NAME = "Minecraft Blueprints";

    public static final Gson GSON = new Gson();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ScreenHandlerType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "blueprint"),
            new ScreenHandlerType<>(BlueprintScreenHandler::new, FeatureSet.empty())
    );

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}...", MOD_NAME);

        LOGGER.info("Initializing BlueprintScheduler...");
        BlueprintScheduler.getInstance();

        register_nodes();

        LOGGER.info("Registering {}...", BlueprintWorkbench.ID);
        final Identifier workbench_id = Identifier.of(MOD_ID, BlueprintWorkbench.ID);
        final RegistryKey<Block> workbench_key = RegistryKey.of(RegistryKeys.BLOCK, workbench_id);

        final Block blueprint_workbench = new BlueprintWorkbench(
                Block.Settings.copy(Blocks.CRAFTING_TABLE).nonOpaque().registryKey(workbench_key)
        );
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, BlueprintWorkbench.ID), blueprint_workbench);

        final RegistryKey<Item> workbench_item_key = RegistryKey.of(RegistryKeys.ITEM, workbench_id);

        final BlockItem workbench_item = new BlockItem(blueprint_workbench, new Item.Settings().registryKey(workbench_item_key));
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, BlueprintWorkbench.ID), workbench_item);

        LOGGER.info("Registering events...");
        register_events();

        LOGGER.info("{} has been successfully initialized.", MOD_NAME);
	}

    private void register_nodes() {
        LOGGER.info("Registering blueprint nodes...");

        // Event nodes
        NodeRegistry.register("on_block_use", OnBlockUseEventNode.FACTORY);
        NodeRegistry.register("on_server_tick", OnServerTickEventNode.FACTORY);
        NodeRegistry.register("on_player_join", OnPlayerJoinEventNode.FACTORY);
        NodeRegistry.register("on_block_break", OnBlockBreakEventNode.FACTORY);

        // Flow control nodes
        NodeRegistry.register("branch", BranchNode.FACTORY);
        NodeRegistry.register("sequence", SequenceNode.FACTORY);
        NodeRegistry.register("for_loop", ForLoopNode.FACTORY);
        NodeRegistry.register("while_loop", WhileLoopNode.FACTORY);

        // Action nodes
        NodeRegistry.register("print", PrintNode.FACTORY);
        NodeRegistry.register("delay", DelayNode.FACTORY);

        // Variable nodes
        NodeRegistry.register("get_variable", GetVariableNode.FACTORY);
        NodeRegistry.register("set_variable", SetVariableNode.FACTORY);

        // Math nodes
        NodeRegistry.register("add", AddNode.FACTORY);
        NodeRegistry.register("subtract", SubtractNode.FACTORY);
        NodeRegistry.register("multiply", MultiplyNode.FACTORY);
        NodeRegistry.register("divide", DivideNode.FACTORY);

        LOGGER.info("Registered {} blueprint nodes.", NodeRegistry.get_all_node_ids().size());
    }

    private void register_events() {
        final BlueprintExecutor executor = new BlueprintExecutor();

        // On Block Use event
        UseBlockCallback.EVENT.register((player, world, hand, hit_result) -> {
            if (world.isClient()) return ActionResult.PASS;

            final List<IBlueprintGraph> blueprints = BlueprintRegistry.get_blueprints_for_event(OnBlockUseEventNode.EVENT_ID);
            for (final IBlueprintGraph blueprint : blueprints) {
                final IExecutionContext context = new ExecutionContext(world, player);

                // Set event data as pin values
                // Note: Event nodes should set these values during their execute() method
                // For now, we set them in variables
                context.getVariables().set("player", player);
                context.getVariables().set("block_pos", hit_result.getBlockPos());
                context.getVariables().set("world", world);
                context.getVariables().set("hand", hand.name());

                final IExecutionResult result = executor.execute(blueprint, context);
                if (!result.isSuccess()) {
                    LOGGER.error("Blueprint '{}' execution failed: {}", blueprint.getName(), result.error());
                }
            }

            return ActionResult.PASS;
        });

        // On Server Tick event
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            final List<IBlueprintGraph> blueprints = BlueprintRegistry.get_blueprints_for_event(OnServerTickEventNode.EVENT_ID);
            for (final IBlueprintGraph blueprint : blueprints) {
                final IExecutionContext context = new ExecutionContext(null, null);
                context.getVariables().set("server", server.toString());

                final IExecutionResult result = executor.execute(blueprint, context);
                if (!result.isSuccess()) {
                    LOGGER.error("Blueprint '{}' execution failed: {}", blueprint.getName(), result.error());
                }
            }
        });

        // On Player Join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final List<IBlueprintGraph> blueprints = BlueprintRegistry.get_blueprints_for_event(OnPlayerJoinEventNode.EVENT_ID);
            for (final IBlueprintGraph blueprint : blueprints) {
                final IExecutionContext context = new ExecutionContext(null, handler.getPlayer());
                context.getVariables().set("player", handler.getPlayer());
                context.getVariables().set("server", server.toString());

                final IExecutionResult result = executor.execute(blueprint, context);
                if (!result.isSuccess()) {
                    LOGGER.error("Blueprint '{}' execution failed: {}", blueprint.getName(), result.error());
                }
            }
        });

        // On Block Break event
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;

            final List<IBlueprintGraph> blueprints = BlueprintRegistry.get_blueprints_for_event(OnBlockBreakEventNode.EVENT_ID);
            for (final IBlueprintGraph blueprint : blueprints) {
                final IExecutionContext context = new ExecutionContext(world, player);
                context.getVariables().set("player", player);
                context.getVariables().set("block_pos", pos);
                context.getVariables().set("block_state", state);
                context.getVariables().set("world", world);

                final IExecutionResult result = executor.execute(blueprint, context);
                if (!result.isSuccess()) {
                    LOGGER.error("Blueprint '{}' execution failed: {}", blueprint.getName(), result.error());
                }
            }

            return true;
        });

        LOGGER.info("Registered {} Fabric event handlers for blueprint execution.", 5);
    }
}