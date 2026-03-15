package com.benca.sprinklermod.growth;

/**
 * Maps a fluid type to a plant growth speed multiplier.
 *
 * This class is PURE LOGIC — it has no Minecraft world interaction,
 * no block placing, no entity spawning. Just input → output.
 *
 * WHY THIS IS SEPARATE:
 *   Keeping growth multiplier logic here means you can:
 *   - Test it without launching Minecraft
 *   - Add new fluids in one place without touching sprinkler logic
 *   - Read exactly what each fluid does at a glance
 *
 * HOW TO ADD A NEW FLUID:
 *   1. Register the fluid in FluidRegistry (Ill'll build that later)
 *   2. Add a constant below describing its multiplier
 *   3. Add a case in getMultiplier()
 *   That's it. The sprinkler logic picks it up automatically.
 *
 * NOTE ON FLUID REFERENCES:
 *   Right now we use String identifiers as placeholders because
 *   FluidRegistry doesn't exist yet. When FluidRegistry is built,
 *   we swap the String parameter for the actual Fluid type.
 *   That swap happens in ONE place — getMultiplier() below.
 *
 * CODE SMELL WARNING:
 *   String-based fluid identification is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */
public class GrowthHandler {

    // -------------------------------------------------------------------------
    // Growth multiplier constants
    // Defined here so they are readable and easy to tweak without
    // hunting through logic code
    // -------------------------------------------------------------------------

    /** No fluid supplied — sprinkler does nothing */
    public static final float MULTIPLIER_NONE           = 0.0f;

    /** Plain water — modest growth boost */
    public static final float MULTIPLIER_WATER          = 1.0f;

    /** Fertilised water (bonemeal + water) — strong growth boost */
    public static final float MULTIPLIER_FERTILISED     = 20.0f;

    // -------------------------------------------------------------------------
    // Fluid identifier constants (temporary placeholders)
    // These will be replaced with actual Fluid references when
    // FluidRegistry is built. Only change these strings here —
    // nothing else in the codebase should hardcode fluid names.
    // -------------------------------------------------------------------------

    /** Identifier for plain water */
    public static final String FLUID_WATER          = "water";

    /** Identifier for fertilised water */
    public static final String FLUID_FERTILISED     = "fertilised_water";

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------

    /**
     * Returns the growth multiplier for a given fluid identifier.
     *
     * A multiplier of 2.0 means plants grow twice as fast.
     * A multiplier of 0.0 means no effect.
     *
     * TODO: Replace String parameter with Fluid type when FluidRegistry is built.
     *
     * @param fluidId  The identifier of the fluid being used
     * @return         The growth speed multiplier for that fluid
     */
    public static float getMultiplier(String fluidId) {
        return switch (fluidId) {
            case FLUID_WATER        -> MULTIPLIER_WATER;
            case FLUID_FERTILISED   -> MULTIPLIER_FERTILISED;
            default                 -> MULTIPLIER_NONE;
        };
    }

    /**
     * Convenience method — returns true if the given fluid has any growth effect.
     * Useful for early-exit checks before doing expensive area calculations.
     *
     * @param fluidId  The identifier of the fluid being used
     * @return         True if the fluid provides a growth bonus
     */
    public static boolean hasEffect(String fluidId) {
        return getMultiplier(fluidId) > MULTIPLIER_NONE;
    }
}