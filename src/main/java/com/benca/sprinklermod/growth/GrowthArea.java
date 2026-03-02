package com.benca.sprinklermod.growth;

import java.util.ArrayList;
import java.util.List;

/**
 *  calculate which block positions fall inside a sprinkler's coverage area.
 *
 * pure coordinate math — I know nothing about the Minecraft world,
 * just take a tier and a centre point and return a list of relative positions.
 *
 * WHY SEPARATE:
 *   The sprinkler block entity just asks me "give me the positions" and I hand
 *   them back. If you want to change how area is calculated (e.g. circular
 *   instead of square) you change it here and nowhere else.
 *
 * HOW TO USE:
 *   List<int[]> positions = GrowthArea.getRelativePositions(SprinklerTier.IRON);
 *   Each int[] is [x, z] relative to the sprinkler — so [0,0] is directly below it,
 *   [-1, 1] is one block west and one block north, etc.
 *
 * TODO: If I ever want circular or cross-shaped areas, This is the only file that needs to change.
 */
public class GrowthArea {

    /**
     * return every [x, z] position inside the coverage square for the given tier.
     *
     * The area is always a square centred on [0, 0].
     * Y (height) is not included — the sprinkler block entity decides
     * how far down to look for plants separately.
     *
     * @param tier  The sprinkler tier I am calculating for
     * @return      A list of [x, z] offsets relative to the sprinkler position
     */
    public static List<int[]> getRelativePositions(SprinklerTier tier) {
        List<int[]> positions = new ArrayList<>();
        int radius = tier.radius;

        // Walk every x and z position within the square radius
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                positions.add(new int[]{x, z});
            }
        }

        return positions;
    }

    /**
     * I return the total number of blocks I cover for a given tier.
     * Useful for debugging or displaying coverage info in a tooltip.
     *
     * @param tier  The sprinkler tier
     * @return      Total block count inside the coverage area
     */
    public static int getAreaBlockCount(SprinklerTier tier) {
        int side = tier.getAreaSideLength();
        return side * side;
    }
}