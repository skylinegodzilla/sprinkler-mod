package com.benca.sprinklermod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * I am the item form of the tank block.
 *
 * I extend BlockItem to add a tooltip showing the tank's
 * capacity per block so players know how to plan their farms.
 *
 * TODO: This is also where the TooltipHelper code smell from
 *   SprinklerTier.toString() should eventually be resolved —
 *   a shared TooltipHelper utility class would serve both.
 */
public class TankItem extends BlockItem {

    public TankItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    /**
     * I add capacity information to the item tooltip.
     * Players can see at a glance how many Droplets each block holds
     * and how many sprinklers one water input can sustain.
     *
     * @param stack   The item stack
     * @param context The tooltip context
     * @param tooltip The tooltip lines to add to
     * @param flag    Whether advanced tooltips are shown
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.literal(
                com.benca.sprinklermod.blockentity.TankBlockEntity.CAPACITY_PER_BLOCK
                        + " Droplets per block"));
        tooltip.add(Component.literal(
                "1 water input sustains 3 sprinklers"));
        tooltip.add(Component.literal(
                "Right click to check current level"));
    }
}