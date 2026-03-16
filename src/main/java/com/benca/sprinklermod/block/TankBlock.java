package com.benca.sprinklermod.block;

import com.benca.sprinklermod.blockentity.BlockEntityRegistry;
import com.benca.sprinklermod.blockentity.TankBlockEntity;
import com.benca.sprinklermod.tank.TankMultiblockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * I am the physical tank block that players place to build the tank structure.
 *
 * I am intentionally thin — my job is to:
 *   - Exist in the world with the right properties
 *   - Trigger multiblock validation when placed or broken
 *   - Create and provide my block entity
 *   - Forward tick calls to my block entity
 *   - Show fluid level when right clicked
 *
 * HOW MULTIBLOCK WORKS:
 *   When I am placed I ask TankMultiblockValidator if the surrounding
 *   tank blocks form a valid rectangle. If they do I tell the master
 *   block entity to take ownership of the shared fluid pool.
 *   When I am broken I tell the master to invalidate the structure.
 *
 * VISUAL STATES (TODO — when models are built):
 *   Blockstate variants for connection directions so connected tanks
 *   look like a joined structure visually.
 */
public class TankBlock extends BaseEntityBlock {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public TankBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Multiblock detection
    // -------------------------------------------------------------------------

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                net.minecraft.world.level.block.Block neighborBlock,
                                BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) return;
        validateAndUpdateStructure(level, pos);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) return;
        validateAndUpdateStructure(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof TankBlockEntity tankEntity) {
                tankEntity.invalidateStructure((ServerLevel) level);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // -------------------------------------------------------------------------
    // Right click — show fluid level
    // -------------------------------------------------------------------------

    /**
     * I am called when the player right clicks me without an item.
     * I display the current fluid level as a chat message.
     */
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
                                            BlockPos pos, Player player,
                                            BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof TankBlockEntity tankEntity) {

            TankBlockEntity master = tankEntity;
            BlockPos masterPos = tankEntity.getMasterPos();
            if (!tankEntity.isMaster() && masterPos != null) {
                if (level.getBlockEntity(masterPos) instanceof TankBlockEntity m) {
                    master = m;
                }
            }

            if (!master.isPartOfStructure()) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Tank structure invalid — must be a solid rectangle"));
            } else {
                int current = master.getFluidUnits();
                int capacity = master.getTotalCapacity();
                int percent = capacity > 0 ? (current * 100) / capacity : 0;
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "Tank: " + current + "/" + capacity
                                        + " Droplets (" + percent + "% full)"));
            }
        }

        return InteractionResult.SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Structure validation helper
    // -------------------------------------------------------------------------

    private void validateAndUpdateStructure(Level level, BlockPos pos) {
        TankMultiblockValidator.ValidationResult result =
                TankMultiblockValidator.validate(level, pos);

        if (result.isValid) {
            for (BlockPos structurePos : result.positions) {
                if (level.getBlockEntity(structurePos) instanceof TankBlockEntity tankEntity) {
                    tankEntity.setMasterPos(result.masterPos);
                }
            }
            if (level.getBlockEntity(result.masterPos) instanceof TankBlockEntity master) {
                master.setStructureSize(result.positions.size());
            }
        } else {
            if (level.getBlockEntity(pos) instanceof TankBlockEntity tankEntity) {
                tankEntity.clearMasterPos();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Block entity wiring
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        if (type == BlockEntityRegistry.TANK.get()) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<TankBlockEntity>)
                    (serverLevel, pos, blockState, blockEntity) ->
                            blockEntity.tick((ServerLevel) serverLevel, pos);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }
}