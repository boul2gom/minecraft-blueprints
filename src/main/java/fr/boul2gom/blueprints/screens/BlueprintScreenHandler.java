package fr.boul2gom.blueprints.screens;

import fr.boul2gom.blueprints.MinecraftBlueprints;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;

import static fr.boul2gom.blueprints.MinecraftBlueprints.LOGGER;

public class BlueprintScreenHandler extends ScreenHandler {

    public static final ScreenHandlerFactory FACTORY = (sync_id, inventory, player) -> new BlueprintScreenHandler(sync_id, inventory);

    public BlueprintScreenHandler(int sync_id, PlayerInventory inventory) {
        super(MinecraftBlueprints.BLUEPRINT_SCREEN_HANDLER, sync_id);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void save() {
        LOGGER.info("Saving blueprint...");
    }
}
