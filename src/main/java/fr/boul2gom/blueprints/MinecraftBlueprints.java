package fr.boul2gom.blueprints;

import com.google.gson.Gson;
import fr.boul2gom.blueprints.api.execution.IBlueprintExecutor;
import fr.boul2gom.blueprints.api.execution.IBlueprintScheduler;
import fr.boul2gom.blueprints.api.execution.context.ExecutionContext;
import fr.boul2gom.blueprints.api.execution.context.IExecutionContext;
import fr.boul2gom.blueprints.api.execution.IExecutionResult;
import fr.boul2gom.blueprints.api.graph.IBlueprintGraph;
import fr.boul2gom.blueprints.api.node.IBlueprintNode;
import fr.boul2gom.blueprints.api.provider.ProviderRegistry;
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class MinecraftBlueprints implements ModInitializer {

	public static final String MOD_ID = "minecraft-blueprints";
    public static final String MOD_NAME = "Minecraft Blueprints";

    public static final Gson GSON = new Gson();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ScreenHandlerType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER, Identifier.of(MOD_ID, "blueprint"),
            new ScreenHandlerType<>(BlueprintScreenHandler::new, FeatureSet.empty())
    );

    public static final MinecraftBlueprints INSTANCE = ProviderRegistry.get(MinecraftBlueprints.class);

    private MinecraftServer server;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}...", MOD_NAME);

        // Register providers FIRST, before anything else
        LOGGER.info("Registering service providers...");
        this.register_providers();

        LOGGER.info("Registering server lifecycle events...");
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.server = server;
            LOGGER.info("Server instance is available: {}", server.getName());
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            this.server = null;
            LOGGER.info("Server instance is unavailable");
        });

        LOGGER.info("Registering blueprint nodes...");
        this.register_nodes();

        LOGGER.info("Registering items and blocks...");
        this.register_items();

        LOGGER.info("Registering events...");
        this.register_events();

        LOGGER.info("{} has been successfully initialized.", MOD_NAME);
	}

    public MinecraftServer getServer() {
        return this.server;
    }

    /**
     * Registers all service providers using the ProviderRegistry.
     * This must be called BEFORE any code tries to access Interface.INSTANCE.
     */
    private void register_providers() {
        ProviderRegistry.register(MinecraftBlueprints.class, MinecraftBlueprints::new);
        LOGGER.info("Registered provider: MinecraftBlueprints");

        ProviderRegistry.register(IBlueprintScheduler.class, BlueprintScheduler::new);
        LOGGER.info("Registered provider: IBlueprintScheduler");

        ProviderRegistry.register(IBlueprintExecutor.class, BlueprintExecutor::new);
        LOGGER.info("Registered provider: IBlueprintExecutor");

        LOGGER.info("All service providers registered successfully");
    }

    private void register_nodes() {
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
    
    private void register_items() {
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
        
        LOGGER.info("Registered {} item.", BlueprintWorkbench.ID);
    }

    /**
     * Common pattern for executing blueprints triggered by events.
     * Reduces code duplication across event handlers.
     *
     * @param eventId the event identifier (exception.g., "on_block_use")
     * @param executor the blueprint executor
     * @param context creates ExecutionContext for the event
     * @param eventNodeClass the expected event node class for type checking
     * @param pinConfigurator configures output pins on the event node
     */
    private <T extends IBlueprintNode> void execute_event_blueprint(
            String eventId,
            IBlueprintExecutor executor,
            IExecutionContext context,
            Class<T> eventNodeClass,
            Consumer<T> pinConfigurator
    ) {
        final List<IBlueprintGraph> blueprints = BlueprintRegistry.get_blueprints_for_event(eventId);
        for (final IBlueprintGraph blueprint : blueprints) {
            // Find event node with type safety
            final IBlueprintNode event_node = blueprint.get_entry_points().stream()
                .filter(eventNodeClass::isInstance)
                .findFirst()
                .orElse(null);

            if (event_node != null) {
                // Configure event node pins
                pinConfigurator.accept(eventNodeClass.cast(event_node));
            }

            // Execute blueprint
            final IExecutionResult result = executor.execute(blueprint, context);
            if (!result.isSuccess()) {
                LOGGER.error("Blueprint '{}' execution failed: {}", blueprint.getName(), result.error());
            }
        }
    }

    private void register_events() {
        final IBlueprintExecutor executor = IBlueprintExecutor.INSTANCE;

        // Combined ServerTick handler with explicit priority order:
        // 1. Tick scheduler FIRST (process delayed tasks)
        // 2. Execute blueprints SECOND (may schedule new tasks)
        // This ensures deterministic execution order
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Priority 1: Process scheduler
            IBlueprintScheduler.INSTANCE.tick_scheduler();

            // Priority 2: Execute OnServerTick blueprints
            try (final IExecutionContext context = new ExecutionContext(null, null)) {
                this.execute_event_blueprint(
                    OnServerTickEventNode.EVENT_ID,
                    executor,
                    context,
                    OnServerTickEventNode.class,
                    node -> EventNodeHelper.builder(node, context)
                        .pin("server", server.toString())
                        .apply()
                );
            } catch (Exception exception) {
                LOGGER.error("Error executing OnServerTick event", exception);
            }
        });

        // On Block Use event
        UseBlockCallback.EVENT.register((player, world, hand, hit_result) -> {
            if (world.isClient()) return ActionResult.PASS;

            try (final IExecutionContext context = new ExecutionContext(world, player)) {
                this.execute_event_blueprint(
                    OnBlockUseEventNode.EVENT_ID,
                    executor,
                    context,
                    OnBlockUseEventNode.class,
                    node -> EventNodeHelper.builder(node, context)
                        .pin("player", player)
                        .pin("block_pos", hit_result.getBlockPos())
                        .pin("world", world)
                        .pin("hand", hand.name())
                        .apply()
                );
            } catch (Exception exception) {
                LOGGER.error("Error executing OnBlockUse event", exception);
            }

            return ActionResult.PASS;
        });

        // On Player Join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            try (final IExecutionContext context = new ExecutionContext(null, handler.getPlayer())) {
                this.execute_event_blueprint(
                    OnPlayerJoinEventNode.EVENT_ID,
                    executor,
                    context,
                    OnPlayerJoinEventNode.class,
                    node -> EventNodeHelper.builder(node, context)
                        .pin("player", handler.getPlayer())
                        .pin("server", server.toString())
                        .apply()
                );
            } catch (Exception exception) {
                LOGGER.error("Error executing OnPlayerJoin event", exception);
            }
        });

        // On Block Break event
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;

            try (final IExecutionContext context = new ExecutionContext(world, player)) {
                this.execute_event_blueprint(
                    OnBlockBreakEventNode.EVENT_ID,
                    executor,
                    context,
                    OnBlockBreakEventNode.class,
                    node -> EventNodeHelper.builder(node, context)
                        .pin("player", player)
                        .pin("block_pos", pos)
                        .pin("block_state", state)
                        .pin("world", world)
                        .apply()
                );
            } catch (Exception exception) {
                LOGGER.error("Error executing OnBlockBreak event", exception);
            }

            return true;
        });

        LOGGER.info("Registered {} Fabric event handlers for blueprint execution.", 4);
    }
}