package fr.boul2gom.blueprints;

import fr.boul2gom.blueprints.screens.BlueprintScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

import static fr.boul2gom.blueprints.MinecraftBlueprints.LOGGER;

public class MinecraftBlueprintsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
        LOGGER.info("[Client] Hello Fabric world!");
        HandledScreens.register(MinecraftBlueprints.BLUEPRINT_SCREEN_HANDLER, BlueprintScreen::new);
	}
}