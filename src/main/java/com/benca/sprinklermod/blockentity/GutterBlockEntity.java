package com.benca.sprinklermod.blockentity;

import com.benca.sprinklermod.growth.GrowthHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;

/**
 * I am the memory attached to every gutter block.
 *
 * I remember two things:
 *   - Which fluid is currently flowing through me
 *   - Whether I received fluid this tick (used for propagation)
 *
 * HOW FLUID PROPAGATION WORKS:
 *   Each tick I check my sources in this priority order:
 *     1. Did the gutter directly above me pass fluid down to me?
 *     2. Did the gutter directly behind me (opposite of my facing) pass fluid to me?
 *   If I received fluid I pass it forward:
 *     - To the gutter in front of me (my facing direction only)
 *     - Down to the gutter directly below me (waterfall)
 *     - Down to any sprinkler directly below me
 *
 * THE PROPAGATION CHAIN PATTERN:
 *   No gutter needs to know where the tank or source is.
 *   Each gutter just asks "did I receive fluid this tick?"
 *   and if yes, passes it forward in its facing direction.
 *   Fluid can only flow one way through each gutter — no feedback loops.
 *
 * DIRECTIONAL FLOW:
 *   Each gutter has a FACING direction set when placed.
 *   Fluid enters from the opposite of FACING and exits through FACING.
 *   This prevents feedback loops in branching gutter networks.
 *
 * CODE SMELL WARNING:
 *   String fluid identifier is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */

public class GutterBlockEntity extends BlockEntity {

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** I count ticks so I only process fluid every N ticks instead of every tick */
    private int tickCounter = 0;

    /** I only propagate fluid and grow plants every this many ticks */
    /**
     * I store the fluid currently flowing through me.
     * Empty string means no fluid — sprinkler below me does nothing.
     *
     * CODE SMELL: String placeholder — replace with Fluid type
     * when FluidRegistry is built.
     */
    private String currentFluidId = "";

    /**
     * I track whether I received fluid this tick.
     * This is reset at the start of each tick and set when
     * a source passes fluid to me. It prevents infinite loops
     * in the propagation chain.
     */
    private boolean receivedFluidThisTick = false;

    /** I store a reference to the tank supplying this chain */
    private BlockPos supplyingTankPos = null;

    public BlockPos getSupplyingTankPos() {
        return supplyingTankPos;
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public GutterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.GUTTER.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Fluid reception — called by neighbours passing fluid to me
    // -------------------------------------------------------------------------

    /**
     * I am called by an adjacent gutter or tank that wants to
     * pass fluid to me. I store the fluid and mark myself as
     * having received fluid this tick.
     *
     * @param fluidId The fluid being passed to me
     */
    public void receiveFluid(String fluidId) {
        this.currentFluidId = fluidId;
        this.receivedFluidThisTick = true;
    }

    /**
     * I return the fluid currently flowing through me.
     * Empty string means no fluid.
     *
     * @return The current fluid identifier
     */
    public String getCurrentFluidId() {
        return currentFluidId;
    }

    // -------------------------------------------------------------------------
    // Tick logic — called every game tick by GutterBlock
    // -------------------------------------------------------------------------

    /**
     * I am called every tick by GutterBlock.
     *
     * I check my sources, accept fluid if available, then
     * propagate it forward and downward.
     *
     * @param level The server world
     * @param pos   My position
     */
    public void tick(net.minecraft.server.level.ServerLevel level, BlockPos pos) {

        // Reset received state at the start of each tick
        receivedFluidThisTick = false;

        // Step 1 — check if a tank directly above me has fluid
        checkTankAbove(level, pos);

        // Step 2 — check if the gutter directly above me passed fluid down
        checkGutterAbove(level, pos);

        // Step 3 — check if a gutter behind me passed fluid forward
        checkGuttersBehind(level, pos);

        // Step 4 — if I received fluid, propagate it forward and downward
        if (receivedFluidThisTick && !currentFluidId.isEmpty()) {
            propagateForward(level, pos);
            propagateDown(level, pos);
            feedSprinklerBelow(level, pos);
        } else {
            // No fluid received — clear my fluid so I stop passing it on
            currentFluidId = "";
        }

        // Sync fluid state to client so particles work
        setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }

    // -------------------------------------------------------------------------
    // Source checks
    // -------------------------------------------------------------------------

    /**
     * I check if there is a tank block directly above me with fluid.
     * If there is, I accept fluid from it as my source.
     *
     * @param level The server wForld
     * @param pos   My position
     */
    private void checkTankAbove(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        if (receivedFluidThisTick) return;

        BlockPos abovePos = pos.above();
        if (level.getBlockEntity(abovePos) instanceof TankBlockEntity tank) {
            // Always delegate to master — members don't own the fluid pool
            TankBlockEntity master = tank;
            if (!tank.isMaster() && tank.getMasterPos() != null) {
                if (level.getBlockEntity(tank.getMasterPos()) instanceof TankBlockEntity m) {
                    master = m;
                }
            }
            if (master.getFluidUnits() > 0) {
                receiveFluid(GrowthHandler.FLUID_WATER);
                supplyingTankPos = master.getBlockPos();
            }
        }
    }

    /**
     * I check if the gutter directly above me has fluid and
     * is passing it downward (waterfall behaviour).
     *
     * @param level The server world
     * @param pos   My position
     */
    private void checkGutterAbove(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        if (receivedFluidThisTick) return;

        BlockPos abovePos = pos.above();
        if (level.getBlockEntity(abovePos) instanceof GutterBlockEntity gutterAbove) {
            if (gutterAbove.receivedFluidThisTick) {
                receiveFluid(gutterAbove.getCurrentFluidId());
                supplyingTankPos = gutterAbove.getSupplyingTankPos();
            }
        }
    }

    /**
     * I check the block directly behind me (opposite of my facing direction)
     * for a gutter that received fluid this tick.
     *
     * By only accepting from one specific direction I prevent feedback loops —
     * fluid can only travel through me in one direction.
     *
     * @param level The server world
     * @param pos   My position
     */
    private void checkGuttersBehind(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        if (receivedFluidThisTick) return;

        Direction facing = getFacing(level, pos);
        Direction inputDirection = facing.getOpposite();
        BlockPos inputPos = pos.relative(inputDirection);

        if (level.getBlockEntity(inputPos) instanceof GutterBlockEntity neighbourGutter) {
            if (neighbourGutter.receivedFluidThisTick) {
                receiveFluid(neighbourGutter.getCurrentFluidId());
                supplyingTankPos = neighbourGutter.getSupplyingTankPos();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Propagation
    // -------------------------------------------------------------------------

    /**
     * I pass my fluid to the gutter directly in front of me (my facing direction).
     *
     * By only passing in one specific direction I prevent feedback loops —
     * fluid can only travel through me in one direction.
     *
     * @param level The server world
     * @param pos   My position
     */

    private void propagateForward(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        // Only pass fluid in my facing direction
        Direction facing = getFacing(level, pos);
        BlockPos outputPos = pos.relative(facing);

        if (level.getBlockEntity(outputPos) instanceof GutterBlockEntity neighbourGutter) {
            if (!neighbourGutter.receivedFluidThisTick) {
                neighbourGutter.receiveFluid(currentFluidId);
            }
        }
    }

    /**
     * I pass my fluid down to the gutter directly below me.
     * This creates the waterfall behaviour — fluid cascades
     * down through stacked gutters.
     *
     * @param level The server world
     * @param pos   My position
     */
    private void propagateDown(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        if (level.getBlockEntity(belowPos) instanceof GutterBlockEntity gutterBelow) {
            gutterBelow.receiveFluid(currentFluidId);
        }
    }

    /**
     * I feed my fluid to any sprinkler block directly below me.
     *
     * TODO: Replace with SprinklerBlockEntity.receiveFluid() call
     * when SprinklerBlockEntity is built. For now I directly call
     * PlantGrowthTicker as a temporary stub.
     *
     * @param level The server world
     * @param pos   My position
     */
    private void feedSprinklerBelow(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        if (level.getBlockEntity(belowPos) instanceof SprinklerBlockEntity sprinklerEntity) {
            sprinklerEntity.receiveFluid(currentFluidId);
            sprinklerEntity.setSupplyingTankPos(supplyingTankPos);
        }
    }

    /**
     * I return the direction fluid flows out of me by reading
     * my block's facing from the world.
     *
     * @param level The world
     * @param pos   My position
     * @return      The direction fluid flows out
     */
    private Direction getFacing(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(com.benca.sprinklermod.block.GutterBlock.FACING)) {
            return state.getValue(com.benca.sprinklermod.block.GutterBlock.FACING);
        }
        return Direction.SOUTH; // fallback
    }

    // -------------------------------------------------------------------------
    // NBT persistence — save and load state when chunk unloads/loads
    // -------------------------------------------------------------------------

    /**
     * I save my fluid state to NBT so it persists when the chunk unloads.
     * Without this the gutter forgets its fluid every time the player
     * moves away and the chunk unloads.
     *
     * @param tag The NBT tag to save into
     */
    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("fluidId", currentFluidId);
    }

    /**
     * I load my fluid state from NBT when the chunk reloads.
     *
     * @param tag The NBT tag to load from
     */
    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentFluidId = tag.getString("fluidId");
    }

    /**
     * I tell Minecraft to send my data to the client when I change.
     * Without this the client cannot read my fluidId for particle effects.
     */
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * I provide the data to send to the client in the update packet.
     *
     * @param registries The registry lookup provider
     * @return           My NBT data to send to the client
     */
    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}