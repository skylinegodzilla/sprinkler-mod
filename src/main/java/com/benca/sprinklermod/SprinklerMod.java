package com.benca.sprinklermod;

import com.benca.sprinklermod.block.BlockRegistry;
import com.benca.sprinklermod.item.ItemRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.benca.sprinklermod.blockentity.BlockEntityRegistry;

/**
 * I am the main entry point for the Sprinkler Mod.
 *
 * I am kept intentionally thin — my only job is to assemble
 * the modules together at startup. I do not contain any game
 * logic myself.
 *
 * HOW TO ADD A NEW MODULE:
 *   1. Build and test the module in isolation
 *   2. Add a single register() call here
 *   That's it. Everything else is self-contained in the module.
 *
 * CURRENT MODULES:
 *   - BlockRegistry — sprinkler and gutter blocks
 *
 * PLANNED MODULES:
 *   - ItemRegistry — block items and creative tab
 *   - FluidRegistry — water and fertilised water
 *   - BlockEntityRegistry — gutter and sprinkler block entities
 */
@Mod(SprinklerMod.MOD_ID)
public class SprinklerMod {

    public static final Logger LOGGER = LogUtils.getLogger();

    /** I am the mod ID — I must match neoforge.mods.toml exactly */
    public static final String MOD_ID = "sprinklermod";

    /**
     * I am called by NeoForge when the mod is first loaded.
     * I register all modules with the event bus and nothing else.
     *
     * @param modEventBus  The mod-specific event bus from NeoForge
     * @param modContainer The mod container from NeoForge
     */
    public SprinklerMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the block module
        BlockRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockEntityRegistry.register(modEventBus);
    }
}