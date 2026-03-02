package com.benca.sprinklermod.client;

/**
 * I define the colours used for sprinkler particles and handle
 * gradient interpolation between the centre and edge of the spray.
 *
 * I am separate from the particle handler so colour logic lives
 * in one place — if I want to change a colour I only change it here.
 *
 * HOW GRADIENTS WORK:
 *   I lerp (linear interpolate) between two colours based on a
 *   0.0 to 1.0 progress value. 0.0 = centre colour, 1.0 = edge colour.
 *
 * HOW TO ADD A NEW FLUID COLOUR:
 *   1. Add centre and edge colour constants below
 *   2. Add a case in getColour() returning those constants
 *   That's it.
 */
public class SprinklerParticleColour {

    // -------------------------------------------------------------------------
    // Colour definitions — R, G, B as floats between 0.0 and 1.0
    // -------------------------------------------------------------------------

    // Water — bright cyan/white centre fading to deep blue edge
    private static final float[] WATER_CENTRE = { 0.8f, 1.0f, 1.0f };
    private static final float[] WATER_EDGE    = { 0.0f, 0.3f, 0.9f };

    // Fertilised water — bright yellow/white centre fading to deep green edge
    private static final float[] FERTILISED_CENTRE = { 0.9f, 1.0f, 0.7f };
    private static final float[] FERTILISED_EDGE   = { 0.0f, 0.6f, 0.1f };

    // Default — white, used as fallback if fluid is unknown
    private static final float[] DEFAULT_CENTRE = { 1.0f, 1.0f, 1.0f };
    private static final float[] DEFAULT_EDGE   = { 0.8f, 0.8f, 0.8f };

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * I return an interpolated RGB colour for a particle based on its
     * fluid type and how far from the centre it is.
     *
     * @param fluidId  The fluid identifier from GrowthHandler
     * @param progress 0.0 = sprinkler centre, 1.0 = spray edge
     * @return         float[3] containing R, G, B values between 0.0 and 1.0
     */
    public static float[] getColour(String fluidId, double progress) {
        float[] centre;
        float[] edge;

        switch (fluidId) {
            case "fertilised_water" -> {
                centre = FERTILISED_CENTRE;
                edge   = FERTILISED_EDGE;
            }
            case "water" -> {
                centre = WATER_CENTRE;
                edge   = WATER_EDGE;
            }
            default -> {
                centre = DEFAULT_CENTRE;
                edge   = DEFAULT_EDGE;
            }
        }

        return lerp(centre, edge, (float) progress);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * I linearly interpolate between two RGB colours.
     *
     * @param from     Starting colour (progress = 0.0)
     * @param to       Ending colour (progress = 1.0)
     * @param progress How far between the two colours (0.0 to 1.0)
     * @return         Interpolated RGB float[3]
     */
    private static float[] lerp(float[] from, float[] to, float progress) {
        return new float[] {
                from[0] + (to[0] - from[0]) * progress,
                from[1] + (to[1] - from[1]) * progress,
                from[2] + (to[2] - from[2]) * progress
        };
    }
}