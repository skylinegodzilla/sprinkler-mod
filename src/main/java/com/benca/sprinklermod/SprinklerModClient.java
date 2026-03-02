package com.benca.sprinklermod;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * I handle all client-side setup for the mod.
 *
 * I only load on the client — I will never run on a dedicated server.
 * Any rendering, GUI, or visual code belongs here or in a class I call.
 *
 * Currently I am mostly empty — visual features will be added
 * as the mod develops (particle effects, custom block rendering, etc.)
 */
@Mod(value = SprinklerMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = SprinklerMod.MOD_ID, value = Dist.CLIENT)
public class SprinklerModClient {

    /**
     * I am called when the client loads the mod.
     * I register the config screen so players can access settings
     * from the Mods menu.
     *
     * @param container The mod container provided by NeoForge
     */
    public SprinklerModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * I am called when the Minecraft client finishes setting up.
     * Client-side registration (models, renderers) goes here.
     *
     * @param event The client setup event from NeoForge
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // TODO: Register custom block renderers here when models are added
    }
}