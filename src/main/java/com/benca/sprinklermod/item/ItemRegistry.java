package com.benca.sprinklermod.item;

import com.benca.sprinklermod.SprinklerMod;
import com.benca.sprinklermod.block.BlockRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import com.benca.sprinklermod.item.TankItem;

/**
 * I register every item in the mod with Minecraft's item registry.
 *
 * For every block that exists, there needs to be a matching BlockItem —
 * that is the item form of the block that lives in your inventory.
 * I create those BlockItems here.
 *
 * HOW TO ADD A NEW BLOCK ITEM:
 *   1. Register the block in BlockRegistry
 *   2. Add a matching BlockItem here following the same pattern
 *   3. Add it to the creative tab in CreativeTabRegistry
 *   That's it.
 */
public class ItemRegistry {

    // -------------------------------------------------------------------------
    // Deferred register — I queue items for registration at startup
    // -------------------------------------------------------------------------

    /** I am the item registry queue for this mod */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, SprinklerMod.MOD_ID);

    // -------------------------------------------------------------------------
    // Block items — one per block in BlockRegistry
    // -------------------------------------------------------------------------

    /** I am the item form of the copper sprinkler block */
    public static final DeferredHolder<Item, BlockItem> COPPER_SPRINKLER_ITEM =
            ITEMS.register("copper_sprinkler",
                    () -> new BlockItem(BlockRegistry.COPPER_SPRINKLER.get(),
                            new Item.Properties()));

    /** I am the item form of the iron sprinkler block */
    public static final DeferredHolder<Item, BlockItem> IRON_SPRINKLER_ITEM =
            ITEMS.register("iron_sprinkler",
                    () -> new BlockItem(BlockRegistry.IRON_SPRINKLER.get(),
                            new Item.Properties()));

    /** I am the item form of the gold sprinkler block */
    public static final DeferredHolder<Item, BlockItem> GOLD_SPRINKLER_ITEM =
            ITEMS.register("gold_sprinkler",
                    () -> new BlockItem(BlockRegistry.GOLD_SPRINKLER.get(),
                            new Item.Properties()));

    /** I am the item form of the diamond sprinkler block */
    public static final DeferredHolder<Item, BlockItem> DIAMOND_SPRINKLER_ITEM =
            ITEMS.register("diamond_sprinkler",
                    () -> new BlockItem(BlockRegistry.DIAMOND_SPRINKLER.get(),
                            new Item.Properties()));

    /** I am the item form of the netherite sprinkler block */
    public static final DeferredHolder<Item, BlockItem> NETHERITE_SPRINKLER_ITEM =
            ITEMS.register("netherite_sprinkler",
                    () -> new BlockItem(BlockRegistry.NETHERITE_SPRINKLER.get(),
                            new Item.Properties()));

    /** I am the item form of the gutter block */
    public static final DeferredHolder<Item, BlockItem> GUTTER_ITEM =
            ITEMS.register("gutter",
                    () -> new BlockItem(BlockRegistry.GUTTER.get(),
                            new Item.Properties()));

    /** I am the item form of the tank block */
    public static final DeferredHolder<Item, TankItem> TANK_ITEM =
            ITEMS.register("tank",
                    () -> new TankItem(BlockRegistry.TANK.get(),
                            new Item.Properties()));

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * I am called from the main mod class to register the ITEMS
     * deferred register with the NeoForge event bus.
     *
     * If I am not called at startup, none of the items above will exist in game.
     *
     * @param modEventBus  The mod-specific event bus from the main mod class
     */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}