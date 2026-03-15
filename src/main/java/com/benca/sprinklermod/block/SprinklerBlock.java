package com.benca.sprinklermod.block;

import com.benca.sprinklermod.blockentity.BlockEntityRegistry;
import com.benca.sprinklermod.blockentity.SprinklerBlockEntity;
import com.benca.sprinklermod.growth.SprinklerTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * I am the physical sprinkler block that sits in the world.
 *
 * I extend BaseEntityBlock because I need a SprinklerBlockEntity
 * to remember which fluid I am receiving from the gutter above.
 *
 * I am intentionally thin — my block entity does all the
 * interesting work. My job is to:
 *   - Exist in the world with the right properties
 *   - Know my tier
 *   - Create and provide my block entity
 *   - Forward tick calls to my block entity
 *   - Play the horse sound easter egg on diamond tier removal
 *
 * HOW TICKING WORKS:
 *   My block entity is ticked every game tick.
 *   GutterBlockEntity above me calls receiveFluid() on my entity.
 *   My entity only grows plants if it received fluid this tick.
 *   No fluid = no growth. The sprinkler is dry.
 */
public class SprinklerBlock extends BaseEntityBlock {

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    /** I store my tier so the block entity can be constructed with it */
    private final SprinklerTier tier;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param tier       The tier of this sprinkler
     * @param properties Standard Minecraft block properties
     */
    public SprinklerBlock(SprinklerTier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * I return my tier so BlockEntityRegistry can pass it to
     * the SprinklerBlockEntity on construction.
     *
     * @return this sprinkler's tier
     */
    public SprinklerTier getTier() {
        return tier;
    }

    // -------------------------------------------------------------------------
    // Block entity wiring
    // -------------------------------------------------------------------------

    /**
     * I create a new SprinklerBlockEntity when placed in the world.
     *
     * @param pos   The position the block was placed at
     * @param state The block state at that position
     * @return      A new SprinklerBlockEntity with my tier
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SprinklerBlockEntity(tier, pos, state);
    }

    /**
     * I provide the ticker for my block entity.
     * Only ticks on the server side.
     *
     * @param level The world
     * @param state My block state
     * @param type  The block entity type being requested
     * @return      A ticker for SprinklerBlockEntity, or null on client
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (level.isClientSide()) return null;

        return createTickerHelper(type, BlockEntityRegistry.SPRINKLER.get(),
                (serverLevel, pos, blockState, blockEntity) ->
                        blockEntity.tick((ServerLevel) serverLevel, pos));
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // -------------------------------------------------------------------------
    // Codec — required by BaseEntityBlock in 1.21.1
    // -------------------------------------------------------------------------

    @Override
    public com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    // -------------------------------------------------------------------------
    // Easter egg
    // -------------------------------------------------------------------------

    /**
     * I play the horse death sound when the diamond sprinkler is removed.
     * Your daughter's idea, not mine.
     *
     * @param state       My block state
     * @param level       The world
     * @param pos         My position
     * @param newState    The block state replacing me
     * @param movedByPiston Whether a piston caused this
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);

        if (!level.isClientSide() && tier == SprinklerTier.DIAMOND) {
            level.playSound(null, pos,
                    SoundEvents.HORSE_DEATH,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.0f);
        }
    }
}