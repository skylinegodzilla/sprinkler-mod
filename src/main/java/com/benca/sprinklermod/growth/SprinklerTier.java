package com.benca.sprinklermod.growth;

/**
 * Defines the four tiers of sprinkler available in the mod.
 *
 * Each tier determines:
 *   - What material is used to craft it (for reference, actual recipes are in RecipeRegistry)
 *   - The radius of the area it affects (radius 1 = 3x3, radius 2 = 5x5, etc.)
 *
 * HOW TO ADD A NEW TIER:
 *   1. Add a new entry below with a name, display name, and radius
 *   2. Add a recipe in RecipeRegistry
 *   3. Add a block variant in BlockRegistry
 *   Everything else (growth logic, area calculation) picks it up automatically.
 *
 * AREA CALCULATION:
 *   The sprinkler affects a square area centred on itself.
 *   Side length = (radius * 2) + 1
 *   So radius 1 = 3x3, radius 2 = 5x5, radius 4 = 9x9, radius 5 = 11x11
 */
public enum SprinklerTier {

    // Name          Display Name    Radius
    COPPER          ("Copper",       1),   // Covers a 3x3 area
    IRON            ("Iron",         2),   // Covers a 5x5 area
    GOLD            ("Gold",         3),   // Covers a 7x7 area
    DIAMOND         ("Diamond",      4),   // Covers a 9x9 area
    NETHERITE       ("Netherite",    5);   // Covers a 11x11 area

    // -------------------------------------------------------------------------
    // Properties set at construction time — immutable after that
    // -------------------------------------------------------------------------

    /** Human-readable name used in tooltips and display */
    public final String displayName;

    /** Radius in blocks from the sprinkler centre (not including the centre block) */
    public final int radius;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    SprinklerTier(String displayName, int radius) {
        this.displayName = displayName;
        this.radius = radius;
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Returns the side length of the area this tier covers.
     * Example: radius 1 → side 3 (a 3x3 grid)
     *
     * @return the number of blocks along one side of the coverage area
     */
    public int getAreaSideLength() {
        return (radius * 2) + 1;
    }

    /**
     * Returns a readable summary of this tier for debugging or tooltips.
     * Example: "Iron Sprinkler (3x3)"
     *
     * @return formatted string describing this tier
     */
    @Override
    public String toString() {
        int side = getAreaSideLength();
        return displayName + " Sprinkler (" + side + "x" + side + ")";
    }
}