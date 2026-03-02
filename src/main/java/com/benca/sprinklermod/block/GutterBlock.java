package com.benca.sprinklermod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * I am the gutter block that carries fluid overhead.
 *
 * I have two jobs:
 *   - I accept fluid from an adjacent water source
 *   - I propagate that fluid to connected gutters and
 *     down to any sprinkler connected below me
 *
 * I do NOT store fluid myself — that is GutterBlockEntity's job.
 * I am just the physical block. My entity handles the state.
 *
 * HOW FLUID TRAVELS:
 *   Water source → placed next to gutter → gutter accepts fluid
 *   → propagates horizontally to connected gutters
 *   → feeds any sprinkler directly below
 *
 * TODO: Fluid propagation is stubbed — wired up when GutterBlockEntity is built.
 * TODO: I will need BlockEntityProvider when GutterBlockEntity is built.
 */
public class GutterBlock extends Block {

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * I am constructed with standard block properties.
     * My visual and physical properties are defined in BlockRegistry.
     *
     * @param properties  Standard Minecraft block properties
     */
    public GutterBlock(Properties properties) {
        super(properties);
    }

    // -------------------------------------------------------------------------
    // Minecraft block behaviour
    // -------------------------------------------------------------------------

    /**
     * I am called when a neighbouring block changes.
     *
     * I check if the changed neighbour is a water source —
     * if so I accept the fluid and begin propagating it.
     *
     * TODO: Replace stub with real fluid acceptance logic
     * when GutterBlockEntity is built.
     *
     * @param state        My current block state
     * @param level        The world I am in
     * @param pos          My position in the world
     * @param neighborBlock The block that changed
     * @param neighborPos  The position of the block that changed
     * @param movedByPiston Whether the change was caused by a piston
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        // Only process on the server side
        if (level.isClientSide()) return;

        // Check each horizontal direction for a water source neighbour
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbourState = level.getBlockState(neighbourPos);

            // TODO: Replace this check with proper fluid tag check
            // when FluidRegistry is built
            if (neighbourState.getBlock() == net.minecraft.world.level.block.Blocks.WATER) {
                System.out.println("Gutter at " + pos + " detected water to the " + direction);
                // TODO: GutterBlockEntity.receiveFluid(fluidId) goes here
            }
        }
    }
}