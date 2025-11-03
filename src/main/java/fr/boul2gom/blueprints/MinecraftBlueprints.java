package fr.boul2gom.blueprints;

import com.google.gson.Gson;
import fr.boul2gom.blueprints.blocks.BlueprintWorkbench;
import fr.boul2gom.blueprints.screens.BlueprintScreenHandler;
import fr.boul2gom.blueprints.util.EntityUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, world) -> {
            final Text entity_name = EntityUtils.getDisplayName(entity);
            final Text death_message = entity.getDamageTracker().getDeathMessage();

            LOGGER.info("{} died from {}.", entity_name.getString(), death_message.getString());
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit_result) -> {
            if (world.isClient()) return ActionResult.PASS;

            final Text name = EntityUtils.getDisplayName(player);
            final String result = GSON.toJson(hit_result);

            LOGGER.info("[{}] {} used {} at {}.", hand.name(), name.getString(), result, hit_result.getBlockPos());
            return ActionResult.PASS;
        });


        LOGGER.info("{} has been successfully initialized.", MOD_NAME);
	}
}