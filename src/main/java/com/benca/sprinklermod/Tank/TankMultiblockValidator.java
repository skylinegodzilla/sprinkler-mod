package com.benca.sprinklermod.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * I validate whether a group of tank blocks forms a valid rectangular structure.
 *
 * I am pure logic — no Minecraft world interaction beyond reading block positions.
 * I can be unit tested without launching the game.
 *
 * WHAT MAKES A VALID TANK:
 *   - All blocks must be TankBlocks
 *   - They must form a perfect rectangle (no L shapes, no gaps)
 *   - Valid shapes: 1x1, 2x1, 2x2, 3x2, 3x3 etc
 *
 * HOW I WORK:
 *   1. Starting from a placed block, I flood fill to find all connected tank blocks
 *   2. I check if those blocks form a perfect rectangle
 *   3. I return the result including the bounds and master block position
 *
 * THE MASTER BLOCK:
 *   The master block is always the lowest corner (min X, min Z, min Y).
 *   It owns the shared fluid pool. All other blocks report to it.
 *
 * TODO: Add unit tests in TankModuleTest when this class is complete.
 */
public class TankMultiblockValidator {

    // -------------------------------------------------------------------------
    // Validation result
    // -------------------------------------------------------------------------

    /**
     * I am the result of a validation attempt.
     * I carry either a valid structure description or a failure reason.
     */
    public static class ValidationResult {

        /** True if the structure is a valid rectangle */
        public final boolean isValid;

        /** All block positions that are part of this structure */
        public final List<BlockPos> positions;

        /** The master block position (lowest corner) */
        public final BlockPos masterPos;

        /** Why validation failed — empty if valid */
        public final String failureReason;

        private ValidationResult(boolean isValid, List<BlockPos> positions,
                                 BlockPos masterPos, String failureReason) {
            this.isValid = isValid;
            this.positions = positions;
            this.masterPos = masterPos;
            this.failureReason = failureReason;
        }

        /** I create a successful validation result */
        public static ValidationResult success(List<BlockPos> positions, BlockPos masterPos) {
            return new ValidationResult(true, positions, masterPos, "");
        }

        /** I create a failed validation result */
        public static ValidationResult failure(String reason) {
            return new ValidationResult(false, new ArrayList<>(), null, reason);
        }
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** I am the maximum number of tank blocks allowed in one structure */
    public static final int MAX_TANK_BLOCKS = 9; // 3x3 maximum

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * I validate the tank structure starting from the given position.
     *
     * I flood fill from the starting position to find all connected
     * tank blocks, then check if they form a valid rectangle.
     *
     * @param level       The world
     * @param startPos    The position of the block that was just placed
     * @return            A ValidationResult describing the outcome
     */
    public static ValidationResult validate(Level level, BlockPos startPos) {

        // Step 1 — flood fill to find all connected tank blocks
        List<BlockPos> connected = floodFill(level, startPos);

        if (connected.isEmpty()) {
            return ValidationResult.failure("No tank blocks found");
        }

        if (connected.size() > MAX_TANK_BLOCKS) {
            return ValidationResult.failure("Tank structure too large (max " + MAX_TANK_BLOCKS + " blocks)");
        }

        // Step 2 — check if they form a valid rectangle
        return validateRectangle(connected);
    }

    // -------------------------------------------------------------------------
    // Flood fill
    // -------------------------------------------------------------------------

    /**
     * I find all tank blocks connected horizontally to the starting position.
     * I only search horizontally — tanks are flat structures, not towers.
     *
     * @param level     The world
     * @param startPos  The starting position
     * @return          All connected tank block positions
     */
    private static List<BlockPos> floodFill(Level level, BlockPos startPos) {
        List<BlockPos> found = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> queue = new ArrayList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.remove(0);

            // Check if this position is a tank block
            if (level.getBlockState(current).getBlock()
                    instanceof com.benca.sprinklermod.block.TankBlock) {
                found.add(current);

                // Add horizontal neighbours to the queue
                for (BlockPos neighbour : getHorizontalNeighbours(current)) {
                    if (!visited.contains(neighbour)) {
                        visited.add(neighbour);
                        queue.add(neighbour);
                    }
                }
            }

            // Stop if we have found too many blocks
            if (found.size() > MAX_TANK_BLOCKS) break;
        }

        return found;
    }

    /**
     * I return the four horizontal neighbours of a position.
     * Used for flood fill — tanks only connect horizontally.
     *
     * @param pos The centre position
     * @return    The four horizontal neighbours
     */
    private static List<BlockPos> getHorizontalNeighbours(BlockPos pos) {
        List<BlockPos> neighbours = new ArrayList<>();
        neighbours.add(pos.north());
        neighbours.add(pos.south());
        neighbours.add(pos.east());
        neighbours.add(pos.west());
        return neighbours;
    }

    // -------------------------------------------------------------------------
    // Rectangle validation
    // -------------------------------------------------------------------------

    /**
     * I check if a list of positions forms a perfect rectangle.
     *
     * A perfect rectangle means:
     *   - All blocks are on the same Y level
     *   - The count equals width * length
     *   - No gaps inside the rectangle
     *
     * @param positions The positions to validate
     * @return          A ValidationResult
     */
    private static ValidationResult validateRectangle(List<BlockPos> positions) {

        // Find the bounding box
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        // All blocks must be on the same Y level — tanks are flat
        if (minY != maxY) {
            return ValidationResult.failure("Tank blocks must all be at the same height");
        }

        // Count must equal width * length — no gaps allowed
        int expectedCount = (maxX - minX + 1) * (maxZ - minZ + 1);
        if (positions.size() != expectedCount) {
            return ValidationResult.failure("Tank must be a solid rectangle — no gaps or L-shapes allowed");
        }

        // Master block is the lowest corner (min X, min Z)
        BlockPos masterPos = new BlockPos(minX, minY, minZ);

        return ValidationResult.success(positions, masterPos);
    }
}