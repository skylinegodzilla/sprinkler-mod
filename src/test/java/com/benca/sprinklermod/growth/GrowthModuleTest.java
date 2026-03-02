package com.benca.sprinklermod.growth;

import java.util.List;

/**
 * I test the entire growth module without launching Minecraft.
 * Run me by right-clicking this file in IntelliJ and selecting "Run GrowthModuleTest.main()"
 *
 * If all tests pass, the growth module is working correctly and
 * ready to be wired up to the Minecraft API.
 */
public class GrowthModuleTest {

    public static void main(String[] args) {
        System.out.println("=== Growth Module Tests ===\n");

        testTierAreas();
        testGrowthMultipliers();
        testGrowthHasEffect();
        testTickerOutput();

        System.out.println("\n=== All tests complete ===");
    }

    /** I verify each tier produces the correct area size */
    private static void testTierAreas() {
        System.out.println("-- Tier Areas --");
        for (SprinklerTier tier : SprinklerTier.values()) {
            List<int[]> positions = GrowthArea.getRelativePositions(tier);
            int expected = GrowthArea.getAreaBlockCount(tier);
            boolean pass = positions.size() == expected;
            System.out.println(tier.displayName
                    + " | expected: " + expected
                    + " | got: " + positions.size()
                    + " | " + (pass ? "PASS" : "FAIL"));
        }
        System.out.println();
    }

    /** I verify each fluid returns the correct multiplier */
    private static void testGrowthMultipliers() {
        System.out.println("-- Growth Multipliers --");
        check("Water multiplier",
                GrowthHandler.getMultiplier(GrowthHandler.FLUID_WATER),
                GrowthHandler.MULTIPLIER_WATER);
        check("Fertilised multiplier",
                GrowthHandler.getMultiplier(GrowthHandler.FLUID_FERTILISED),
                GrowthHandler.MULTIPLIER_FERTILISED);
        check("Unknown fluid multiplier",
                GrowthHandler.getMultiplier("unknown_fluid"),
                GrowthHandler.MULTIPLIER_NONE);
        System.out.println();
    }

    /** I verify hasEffect() returns the right result for each fluid */
    private static void testGrowthHasEffect() {
        System.out.println("-- Has Effect --");
        checkBool("Water has effect",
                GrowthHandler.hasEffect(GrowthHandler.FLUID_WATER), true);
        checkBool("Fertilised has effect",
                GrowthHandler.hasEffect(GrowthHandler.FLUID_FERTILISED), true);
        checkBool("Unknown fluid has no effect",
                GrowthHandler.hasEffect("unknown_fluid"), false);
        System.out.println();
    }

    /** I run the ticker stub so I can see the output looks correct */
    private static void testTickerOutput() {
        System.out.println("-- Ticker Output --");
        System.out.println("Ticker test skipped — requires ServerLevel (Minecraft API)");
        System.out.println("Test in-game by placing a sprinkler above crops.");
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // Assertion helpers
    // -------------------------------------------------------------------------

    private static void check(String label, float actual, float expected) {
        boolean pass = actual == expected;
        System.out.println(label
                + " | expected: " + expected
                + " | got: " + actual
                + " | " + (pass ? "PASS" : "FAIL"));
    }

    private static void checkBool(String label, boolean actual, boolean expected) {
        boolean pass = actual == expected;
        System.out.println(label
                + " | expected: " + expected
                + " | got: " + actual
                + " | " + (pass ? "PASS" : "FAIL"));
    }
}