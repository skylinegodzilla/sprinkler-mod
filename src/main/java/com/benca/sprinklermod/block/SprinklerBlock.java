package com.benca.sprinklermod.block;

import com.benca.sprinklermod.SprinklerMod;
import com.benca.sprinklermod.growth.SprinklerTier;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import com.benca.sprinklermod.growth.PlantGrowthTicker;

/**
 * I am the physical sprinkler block that sits in the world.
 *
 * I know two things:
 *   - What tier I am (determines my coverage area)
 *   - How to trigger the growth logic each tick
 *
 * I do NOT contain growth logic myself — I delegate that to
 * PlantGrowthTicker. My only job is to exist in the world
 * and call the right things at the right time.
 *
 * HOW TICKING WORKS:
 *   Minecraft calls randomTick() on blocks occasionally.
 *   I override that and call PlantGrowthTicker instead.
 *   The frequency is controlled by the server's random tick speed.
 *
 * HOW TO ADD BEHAVIOUR:
 *   - New area shapes? Change GrowthArea, not me.
 *   - New fluid effects? Change GrowthHandler, not me.
 *   - New visual effects (particles, sounds)? Add them here in randomTick().
 */
public class SprinklerBlock extends Block {

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    /** I store my tier so I know my coverage area and behaviour */
    private final SprinklerTier tier;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * I am constructed with a tier and standard block properties.
     *
     * Block.Properties controls things like hardness, sound, and tool type.
     * Each tier's properties are defined in BlockRegistry — not here.
     *
     * @param tier        The tier of this sprinkler (COPPER, IRON, etc.)
     * @param properties  Standard Minecraft block properties
     */
    public SprinklerBlock(SprinklerTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * I return my tier so other systems can query my coverage area.
     *
     * @return this sprinkler's tier
     */
    public SprinklerTier getTier() {
        return tier;
    }

    // -------------------------------------------------------------------------
    // Minecraft block behaviour
    // -------------------------------------------------------------------------

    /**
     * I am called by Minecraft on random ticks.
     *
     * I check if I have a fluid supply (via the gutter above me)
     * and if so, I trigger the growth ticker for my area.
     *
     * TODO: Fluid supply check is stubbed — wired up when GutterBlockEntity is built.
     * TODO: Add particle and sound effects here later.
     *
     * @param state  My current block state
     * @param level  The world I am in
     * @param pos    My position in the world
     * @param random Minecraft's random source
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos,
                           RandomSource random) {

        // TODO: Replace stub fluid with real check from GutterBlockEntity
        String fluidId = "water";

        // Delegate all growth logic to PlantGrowthTicker
        PlantGrowthTicker.tickArea(tier, fluidId, level, pos);
    }

    /**
     * I tell Minecraft that I want to receive random ticks.
     * Without this, randomTick() above will never be called.
     *
     * @param state My current block state
     * @return      Always true — I always want random ticks
     */
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        // Play dying horse sound at the block's position
        // ...your daughter's idea, not mine
        if (!level.isClientSide() && tier == SprinklerTier.DIAMOND) {
            SprinklerMod.LOGGER.info("onRemove called for diamond sprinkler");
            level.playSound(null, pos,
                    SoundEvents.HORSE_DEATH,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.0f);
        }
    }
}