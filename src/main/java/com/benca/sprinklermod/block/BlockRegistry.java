package com.benca.sprinklermod.block;

import com.benca.sprinklermod.growth.SprinklerTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * I register every block in the mod with Minecraft's block registry.
 *
 * I am the single source of truth for all block definitions.
 * If you want to add a new block, this is the only place you need to touch
 * (plus a recipe in RecipeRegistry and a model in resources).
 *
 * HOW DEFERRED REGISTRATION WORKS:
 *   NeoForge uses a deferred register system — I queue up my blocks
 *   and NeoForge registers them at the right time during game startup.
 *   I never register blocks directly, I just declare them here and
 *   NeoForge handles the timing.
 *
 * HOW TO ADD A NEW BLOCK:
 *   1. Add a new BLOCKS.register() call below following the same pattern
 *   2. Add a matching item in ItemRegistry
 *   3. Add a model and blockstate in resources
 *   That's it.
 */
public class BlockRegistry {

    // -------------------------------------------------------------------------
    // Deferred register — I queue blocks for registration at startup
    // -------------------------------------------------------------------------

    /**
     * I am the registry queue. All blocks in this mod are registered through me.
     * "sprinklermod" must match the mod ID in neoforge.mods.toml exactly.
     */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, "sprinklermod");

    // -------------------------------------------------------------------------
    // Sprinkler blocks — one per tier
    // -------------------------------------------------------------------------

    /** Copper sprinkler — entry tier, covers a 3x3 area */
    public static final DeferredHolder<Block, SprinklerBlock> COPPER_SPRINKLER =
            BLOCKS.register("copper_sprinkler",
                    () -> new SprinklerBlock(SprinklerTier.COPPER,
                            BlockBehaviour.Properties.of()
                                    .strength(0.1f, 8.0f)
                                    .sound(SoundType.COPPER)
                                    .requiresCorrectToolForDrops()));

    /** Iron sprinkler — covers a 5x5 area */
    public static final DeferredHolder<Block, SprinklerBlock> IRON_SPRINKLER =
            BLOCKS.register("iron_sprinkler",
                    () -> new SprinklerBlock(SprinklerTier.IRON,
                            BlockBehaviour.Properties.of()
                                    .strength(0.5f,8.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()));

    /** Gold sprinkler — covers a 7x7 area */
    public static final DeferredHolder<Block, SprinklerBlock> GOLD_SPRINKLER =
            BLOCKS.register("gold_sprinkler",
                    () -> new SprinklerBlock(SprinklerTier.GOLD,
                            BlockBehaviour.Properties.of()
                                    .strength(0.5f, 8.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()));

    /** Diamond sprinkler — covers a 9x9 area */
    public static final DeferredHolder<Block, SprinklerBlock> DIAMOND_SPRINKLER =
            BLOCKS.register("diamond_sprinkler",
                    () -> new SprinklerBlock(SprinklerTier.DIAMOND,
                            BlockBehaviour.Properties.of()
                                    .strength(1.0f,8.0f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()));

    /** Netherite sprinkler — covers a 11x11 area */
    public static final DeferredHolder<Block, SprinklerBlock> NETHERITE_SPRINKLER =
            BLOCKS.register("netherite_sprinkler",
                    () -> new SprinklerBlock(SprinklerTier.NETHERITE,
                            BlockBehaviour.Properties.of()
                                    .strength(5.0f, 1200.0f)
                                    .sound(SoundType.NETHERITE_BLOCK)
                                    .requiresCorrectToolForDrops()));

    /** Gutter block — carries fluid from water sources to sprinklers */
    public static final DeferredHolder<Block, GutterBlock> GUTTER =
            BLOCKS.register("gutter",
                    () -> new GutterBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(1.5f)
                                    .sound(SoundType.METAL)
                                    .requiresCorrectToolForDrops()));

    /** Tank block — multiblock cauldron structure that stores and feeds fluid */
    public static final DeferredHolder<Block, TankBlock> TANK =
            BLOCKS.register("tank",
                    () -> new TankBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(1.5f, 4.0f)
                                    .sound(SoundType.COPPER)
                                    .requiresCorrectToolForDrops()));

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * I am called from the main mod class to register the BLOCKS
     * deferred register with the NeoForge event bus.
     *
     * If I am not called at startup, none of the blocks above will exist in game.
     *
     * @param modEventBus  The mod-specific event bus from the main mod class
     */
    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}