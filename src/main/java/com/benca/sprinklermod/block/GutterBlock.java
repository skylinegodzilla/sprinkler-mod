package com.benca.sprinklermod.block;

import com.benca.sprinklermod.blockentity.BlockEntityRegistry;
import com.benca.sprinklermod.blockentity.GutterBlockEntity;
import com.benca.sprinklermod.blockentity.SprinklerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;


/**
 * I am the gutter block that carries fluid overhead.
 *
 * I extend BaseEntityBlock because I need a block entity
 * (GutterBlockEntity) to remember which fluid is flowing
 * through me between ticks.
 *
 * I am intentionally thin — my block entity does all the
 * interesting work. My job is just to:
 *   - Exist in the world with the right properties
 *   - Create and provide my block entity
 *   - Forward tick calls to my block entity
 *
 * HOW TICKING WORKS:
 *   Unlike SprinklerBlock which uses random ticks, I use
 *   regular ticks (every game tick) so fluid propagation
 *   is smooth and immediate rather than random.
 *
 * TODO: Performance — consider throttling to every N ticks
 *   if profiling shows gutter ticking is expensive.
 *   Flagged for review when Spark profiling is done.
 */
public class GutterBlock extends BaseEntityBlock {

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    /** I store which direction fluid flows out of me */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * I set my default state to face south when first registered.
     * Call this in the constructor.
     */
    public GutterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    // -------------------------------------------------------------------------
    // Block entity wiring
    // -------------------------------------------------------------------------

    /**
     * I provide the codec used for serialising this block.
     * Required by BaseEntityBlock in 1.21.1.
     * Returns null as we do not need custom block serialisation.
     */
    @Override
    public com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    /**
     * I create a new GutterBlockEntity when a gutter is placed in the world.
     * Minecraft calls this whenever it needs a fresh entity for this block.
     *
     * @param pos   The position the block was placed at
     * @param state The block state at that position
     * @return      A new GutterBlockEntity
     */
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GutterBlockEntity(pos, state);
    }

    /**
     * I provide the ticker that Minecraft uses to tick my block entity.
     *
     * I only return a ticker for the server side — clients do not
     * run fluid propagation logic.
     *
     * @param level     The world
     * @param state     My block state
     * @param type      The block entity type being requested
     * @return          A ticker for GutterBlockEntity, or null on client
     */
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        // Only tick on the server side
        if (level.isClientSide()) return null;

        // Verify the type matches before returning the ticker
        return createTickerHelper(type, BlockEntityRegistry.GUTTER.get(),
                (serverLevel, pos, blockState, blockEntity) ->
                        blockEntity.tick((ServerLevel) serverLevel, pos));
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            // Clear the sprinkler below immediately when this gutter is broken
            BlockPos below = pos.below();
            if (level.getBlockEntity(below) instanceof SprinklerBlockEntity sprinkler) {
                sprinkler.clearFluid();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * I tell Minecraft to render me as a normal model.
     * BaseEntityBlock defaults to INVISIBLE so this must be
     * overridden or the gutter will be invisible in the world.
     *
     * @param state My block state
     * @return      RenderShape.MODEL — render using my block model JSON
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * I face away from the player when placed — fluid flows away from them.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * I register FACING as part of my blockstate.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
                                            BlockPos pos, Player player,
                                            BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Direction facing = state.getValue(FACING);
        String facingName = facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);

        if (level.getBlockEntity(pos) instanceof GutterBlockEntity gutter) {
            String fluid = gutter.getCurrentFluidId().isEmpty() ? "Empty" : "Water";
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "Gutter | Facing: " + facingName + " | Fluid: " + fluid));
        }

        return InteractionResult.SUCCESS;
    }
}