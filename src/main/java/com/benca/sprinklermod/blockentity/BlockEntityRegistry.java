package com.benca.sprinklermod.blockentity;

import com.benca.sprinklermod.SprinklerMod;
import com.benca.sprinklermod.block.BlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * I register every block entity type in the mod with NeoForge.
 *
 * I follow the same deferred registration pattern as BlockRegistry
 * and ItemRegistry — everything is queued here and NeoForge fires
 * registration at the correct moment during startup.
 *
 * HOW TO ADD A NEW BLOCK ENTITY:
 *   1. Build the BlockEntity class in this package
 *   2. Add a DeferredHolder entry here linking it to its block
 *   3. Call register() from SprinklerMod.java
 *   That is it.
 */
public class BlockEntityRegistry {

    // -------------------------------------------------------------------------
    // Deferred register
    // -------------------------------------------------------------------------

    /** I am the block entity registry queue for this mod */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SprinklerMod.MOD_ID);

    // -------------------------------------------------------------------------
    // Block entity types
    // -------------------------------------------------------------------------

    /**
     * I am the block entity type for all gutter blocks.
     * I link GutterBlockEntity to the GutterBlock so Minecraft
     * knows which entity to attach when a gutter is placed.
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GutterBlockEntity>> GUTTER =
            BLOCK_ENTITIES.register("gutter", () ->
                    BlockEntityType.Builder
                            .of(GutterBlockEntity::new, BlockRegistry.GUTTER.get())
                            .build(null));

    /** I am the block entity type for all sprinkler blocks */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SprinklerBlockEntity>> SPRINKLER =
            BLOCK_ENTITIES.register("sprinkler", () ->
                    BlockEntityType.Builder
                            .of(
                                    (pos, state) -> {
                                        // Work out which tier this block is and pass it to the entity
                                        if (state.getBlock() instanceof com.benca.sprinklermod.block.SprinklerBlock sprinkler) {
                                            return new SprinklerBlockEntity(sprinkler.getTier(), pos, state);
                                        }
                                        // Fallback — should never happen
                                        return new SprinklerBlockEntity(com.benca.sprinklermod.growth.SprinklerTier.COPPER, pos, state);
                                    },
                                    BlockRegistry.COPPER_SPRINKLER.get(),
                                    BlockRegistry.IRON_SPRINKLER.get(),
                                    BlockRegistry.GOLD_SPRINKLER.get(),
                                    BlockRegistry.DIAMOND_SPRINKLER.get(),
                                    BlockRegistry.NETHERITE_SPRINKLER.get()
                            )
                            .build(null));

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * I am called from SprinklerMod.java to wire up the deferred register.
     * If I am not called at startup, no block entities will exist in game.
     *
     * @param modEventBus The mod event bus from SprinklerMod
     */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}