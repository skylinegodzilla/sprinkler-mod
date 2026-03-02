package com.benca.sprinklermod.client;

import com.benca.sprinklermod.block.BlockRegistry;
import com.benca.sprinklermod.block.SprinklerBlock;
import com.benca.sprinklermod.growth.PlantGrowthTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * I handle all particle effects for sprinkler blocks on the client side.
 *
 * I listen to the client world tick and for every sprinkler block
 * near the player I spawn water particles raining downward.
 *
 * WHY I AM CLIENT SIDE ONLY:
 *   Particles are purely visual — the server doesn't need to know about them.
 *   Running particle logic on the server would waste resources and cause errors.
 *
 * HOW PARTICLES WORK:
 *   Each tick I find sprinkler blocks near the player and spawn
 *   water drip particles at random positions within the coverage area.
 *   The particles fall downward naturally due to gravity.
 *
 * TODO: When real models are added, spawn particles from the nozzle position
 *   rather than the full coverage area top.
 */
@EventBusSubscriber(modid = "sprinklermod", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SprinklerParticleHandler {
    /** I throttle particle spawning so we don't flood the screen */
    private static int tickCounter = 0;

    /**
     * I am the radius around the player to check for sprinkler blocks.
     * Keeping this small prevents performance issues.
     */
    private static final int PLAYER_CHECK_RADIUS = 16;

    /**
     * I am called every client world tick.
     * I find nearby sprinklers and spawn particles for them.
     *
     * @param event The level tick event from NeoForge
     */
    @SubscribeEvent
    public static void onClientTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!level.isClientSide()) return;

        // Only spawn particles every 3 ticks
        tickCounter++;
        if (tickCounter % 3 != 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // ... rest stays the same

        // Get the player's position as a centre point for our search
        BlockPos playerPos = mc.player.blockPosition();

        // Search nearby blocks for sprinklers
        for (int x = -PLAYER_CHECK_RADIUS; x <= PLAYER_CHECK_RADIUS; x++) {
            for (int y = -PLAYER_CHECK_RADIUS; y <= PLAYER_CHECK_RADIUS; y++) {
                for (int z = -PLAYER_CHECK_RADIUS; z <= PLAYER_CHECK_RADIUS; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    // Check if this block is one of our sprinkler blocks
                    if (state.getBlock() instanceof SprinklerBlock sprinkler) {
                        spawnParticlesForSprinkler(level, checkPos, sprinkler);
                    }
                }
            }
        }
    }

    /**
     * I track the current rotation angle for each sprinkler's particle ring.
     * Each tick the ring rotates, creating a spinning spray effect.
     */
    private static double rotationAngle = 0.7;

    private static void spawnParticlesForSprinkler(Level level,
                                                   BlockPos sprinklerPos,
                                                   SprinklerBlock sprinkler) {
        int radius = sprinkler.getTier().radius;

        // TODO: Replace hardcoded fluidId with real fluid from GutterBlockEntity
        String fluidId = "water";

        // --- Layer 1: Spinning cloud ring from the sprinkler ---
        // Misty cloud look — no colour needed here, just the spray effect
        int sprayCount = 25;
        rotationAngle += 0.25;

        for (int i = 0; i < sprayCount; i++) {
            double angle = rotationAngle + (0.5 * Math.PI * i / sprayCount);

            double ringRadius = 0.8;
            double spawnX = sprinklerPos.getX() + 0.5 + Math.cos(angle) * ringRadius;
            double spawnZ = sprinklerPos.getZ() + 0.5 + Math.sin(angle) * ringRadius;
            double spawnY = sprinklerPos.getY() - 0.3;

            double velocityX = Math.cos(angle) * 0.10 *(radius*0.5);
            double velocityZ = Math.sin(angle) * 0.10 *(radius*0.5);
            double velocityY = -0.05;

            level.addParticle(
                    ParticleTypes.CLOUD,
                    spawnX, spawnY, spawnZ,
                    velocityX, velocityY, velocityZ
            );
        }

        // --- Layer 2: Coloured gradient landing splashes ---
        // Shows coverage area with gradient colour based on fluid type
        // Cyan/white at centre fading to deep blue at edges for water
        for (int i = 0; i < radius * 8; i++) {

            double randomX = sprinklerPos.getX() + 0.5
                    + (level.random.nextDouble() * (radius * 2 + 1)) - radius;
            double randomZ = sprinklerPos.getZ() + 0.5
                    + (level.random.nextDouble() * (radius * 2 + 1)) - radius;

            // Progress based on distance from centre — 0.0 = centre, 1.0 = edge
            double distanceFromCentre = Math.sqrt(
                    Math.pow(randomX - (sprinklerPos.getX() + 0.5), 2) +
                            Math.pow(randomZ - (sprinklerPos.getZ() + 0.5), 2)
            );
            double progress = Math.min(distanceFromCentre / radius, 1.0);
            float[] colour = SprinklerParticleColour.getColour(fluidId, progress);

            BlockPos searchPos = new BlockPos(
                    (int) randomX,
                    sprinklerPos.getY() - 1,
                    (int) randomZ
            );

            for (int dropY = 0; dropY <= PlantGrowthTicker.MAX_DROP_DISTANCE + 2; dropY++) {
                BlockPos checkPos = searchPos.below(dropY);
                if (!level.getBlockState(checkPos).isAir()) {
                    level.addParticle(
                            net.minecraft.core.particles.ColorParticleOption.create(
                                    ParticleTypes.ENTITY_EFFECT,
                                    colour[0], colour[1], colour[2]
                            ),
                            randomX,
                            checkPos.getY() + 1.1,
                            randomZ,
                            0.0, 0.0, 0.0
                    );
                    break;
                }
            }
        }
    }
}