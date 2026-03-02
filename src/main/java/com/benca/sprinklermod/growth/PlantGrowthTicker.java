package com.benca.sprinklermod.growth;

import java.util.List;

/**
 * I apply growth ticks to plants within a sprinkler's coverage area.
 *
 * I am the bridge between the growth math (GrowthArea, GrowthHandler)
 * and the actual Minecraft world. I am the only place in the growth module
 * that will touch the Minecraft API when we wire things up.
 *
 * WHY I AM SEPARATE FROM GrowthHandler:
 *   GrowthHandler knows HOW FAST plants should grow.
 *   I know HOW TO MAKE them grow.
 *   Keeping these separate means I can swap out either without
 *   touching the other.
 *
 * HOW I WORK:
 *   1. I receive a tier (tells me the area) and a fluid (tells me the speed)
 *   2. I ask GrowthArea for the list of positions to affect
 *   3. I ask GrowthHandler for the multiplier
 *   4. I apply that many random ticks to each plant I find
 *
 * TODO: The actual Minecraft world calls are stubbed out below.
 *   When we wire up the block entity, replace the stub comments
 *   with real Level and BlockPos calls.
 *
 * CODE SMELL WARNING:
 *   String fluid identifier is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */
public class PlantGrowthTicker {

    /**
     * I attempt to grow all plants in the sprinkler's coverage area.
     *
     * Right now the Minecraft world calls are stubbed — this method
     * shows the STRUCTURE of what will happen without needing
     * the Minecraft API to compile and test the logic.
     *
     * @param tier      The sprinkler tier (determines coverage area)
     * @param fluidId   The fluid being used (determines growth speed)
     * @param centreX   The X world coordinate of the sprinkler
     * @param centreY   The Y world coordinate of the sprinkler
     * @param centreZ   The Z world coordinate of the sprinkler
     */
    public static void tickArea(SprinklerTier tier, String fluidId,
                                int centreX, int centreY, int centreZ) {

        // Early exit — if this fluid has no effect, do nothing
        if (!GrowthHandler.hasEffect(fluidId)) {
            return;
        }

        // Get how many extra ticks to apply per plant
        float multiplier = GrowthHandler.getMultiplier(fluidId);
        int extraTicks = calculateExtraTicks(multiplier);

        // Get every position in the coverage area
        List<int[]> positions = GrowthArea.getRelativePositions(tier);

        // Apply growth ticks to each position
        for (int[] offset : positions) {
            int worldX = centreX + offset[0];
            int worldZ = centreZ + offset[1];

            // TODO: Replace this stub with real Minecraft world call:
            // BlockPos pos = new BlockPos(worldX, centreY - 1, worldZ);
            // BlockState state = level.getBlockState(pos);
            // if (state.getBlock() instanceof BonemealableBlock growable) {
            //     for (int i = 0; i < extraTicks; i++) {
            //         growable.performBonemeal(level, level.random, pos, state);
            //     }
            // }

            // Temporary stub so we can see the logic working
            System.out.println("Would tick block at: " + worldX + ", " + (centreY - 1) + ", " + worldZ
                    + " | " + extraTicks + " extra ticks | fluid: " + fluidId);
        }
    }

    /**
     * I convert a growth multiplier into a number of extra random ticks.
     *
     * A multiplier of 2.0 means I apply 2 ticks instead of 1.
     * I round down — so 2.9 still means 2 ticks, not 3.
     * If the multiplier is less than 1, I return 1 as the minimum.
     *
     * @param multiplier  The growth speed multiplier from GrowthHandler
     * @return            The number of ticks to apply
     */
    private static int calculateExtraTicks(float multiplier) {
        return Math.max(1, (int) multiplier);
    }
}
