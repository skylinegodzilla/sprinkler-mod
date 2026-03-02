package com.benca.sprinklermod.growth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

/**
 * I apply growth ticks to plants within a sprinkler's coverage area.
 *
 * I am the bridge between the growth math (GrowthArea, GrowthHandler)
 * and the actual Minecraft world.
 *
 * HOW I FIND PLANTS:
 *   For each position in the coverage area, I cast a ray downward
 *   through air blocks up to MAX_DROP_DISTANCE blocks.
 *   When I hit a solid block I check if it's a plant.
 *   If it is, I apply growth ticks to it.
 *
 * WHY DOWNWARD RAYCAST:
 *   This lets the sprinkler work at height — you can build the gutter
 *   system overhead and the sprinkler will still reach crops on the ground
 *   regardless of how high up it is (within MAX_DROP_DISTANCE).
 *
 * TODO: String fluid identifier is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */
public class PlantGrowthTicker {

    /** I am the maximum number of blocks I will search downward for plants */
    public static final int MAX_DROP_DISTANCE = 10;

    /**
     * I attempt to grow all plants beneath the sprinkler's coverage area.
     *
     * For each position in the area I search downward for a plant
     * and apply growth ticks if I find one.
     *
     * @param tier    The sprinkler tier (determines coverage area)
     * @param fluidId The fluid being used (determines growth speed)
     * @param level   The server world
     * @param pos     The position of the sprinkler block itself
     */
    public static void tickArea(SprinklerTier tier, String fluidId,
                                ServerLevel level, BlockPos pos) {

        // Early exit — if this fluid has no effect, do nothing
        if (!GrowthHandler.hasEffect(fluidId)) return;

        // Get how many extra ticks to apply per plant
        int extraTicks = (int) GrowthHandler.getMultiplier(fluidId);

        // Get every horizontal position in the coverage area
        List<int[]> positions = GrowthArea.getRelativePositions(tier);

        for (int[] offset : positions) {
            int worldX = pos.getX() + offset[0];
            int worldZ = pos.getZ() + offset[1];

            // Search downward from directly below the sprinkler
            tryGrowPlantBelow(level, worldX, pos.getY(), worldZ, extraTicks);
        }
    }

    /**
     * I search downward from a given position for a plant to grow.
     *
     * I travel down through air and fluid blocks up to MAX_DROP_DISTANCE.
     * When I hit something solid I check if it is a plant.
     * If it is, I apply the growth ticks.
     *
     * @param level      The server world
     * @param x          World X coordinate to search below
     * @param startY     The Y coordinate to start searching from (the sprinkler's Y)
     * @param z          World Z coordinate to search below
     * @param extraTicks Number of growth ticks to apply
     */
    private static void tryGrowPlantBelow(ServerLevel level,
                                          int x, int startY, int z,
                                          int extraTicks) {

        for (int dropY = 1; dropY <= MAX_DROP_DISTANCE; dropY++) {
            BlockPos checkPos = new BlockPos(x, startY - dropY, z);
            BlockState state = level.getBlockState(checkPos);

            // If this block is air or a fluid, keep searching downward
            if (state.isAir() || !state.getFluidState().is(Fluids.EMPTY)) {
                continue;
            }

            // We hit a solid block — check if it is a plant
            if (state.getBlock() instanceof BonemealableBlock growable) {
                // Check if this plant can actually grow right now
                if (growable.isValidBonemealTarget(level, checkPos, state)) {
                    for (int i = 0; i < extraTicks; i++) {
                        growable.performBonemeal(level, level.random, checkPos, state);
                    }
                }
            }

            // Whether it was a plant or not, stop searching — we hit solid ground
            break;
        }
    }
}