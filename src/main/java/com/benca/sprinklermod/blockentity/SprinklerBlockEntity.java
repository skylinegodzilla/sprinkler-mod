package com.benca.sprinklermod.blockentity;

import com.benca.sprinklermod.growth.PlantGrowthTicker;
import com.benca.sprinklermod.growth.SprinklerTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.benca.sprinklermod.blockentity.TankBlockEntity;
import com.benca.sprinklermod.blockentity.GutterBlockEntity;

/**
 * I am the memory attached to every sprinkler block.
 *
 * I remember one thing:
 *   - Which fluid the gutter above me is currently supplying
 *
 * HOW I WORK:
 *   Each tick GutterBlockEntity checks if there is a sprinkler
 *   below it and calls my receiveFluid() method.
 *   On my own tick I take that fluid and pass it to
 *   PlantGrowthTicker to grow plants beneath me.
 *   If no fluid was received this tick I do nothing —
 *   the sprinkler is dry and has no effect.
 *
 * WHY I EXIST SEPARATELY FROM GutterBlockEntity:
 *   The gutter handles fluid transport.
 *   I handle what to do with the fluid once it arrives.
 *   Keeping these separate means I can be swapped out
 *   or extended without touching the gutter logic.
 *
 * CODE SMELL WARNING:
 *   String fluid identifier is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */
public class SprinklerBlockEntity extends BlockEntity {

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * I store the fluid currently being supplied to me by the gutter above.
     * Empty string means no fluid — I do nothing this tick.
     *
     * CODE SMELL: String placeholder — replace with Fluid type
     * when FluidRegistry is built.
     */
    private String currentFluidId = "";

    /**
     * I track whether I received fluid this tick.
     * Reset at the end of each tick.
     */
    private boolean receivedFluidThisTick = false;

    /**
     * I am the tier of the sprinkler block I am attached to.
     * Set once at construction time from the block itself.
     */
    private final SprinklerTier tier;

    /** I count ticks between growth applications */
    private int tickCounter = 0;

    /** I track how many ticks since I last received fluid */
    private int ticksSinceLastFluid = 0;

    /** If I haven't received fluid in this many ticks I consider myself dry */
    private static final int FLUID_TIMEOUT = 2;

    /** I control how many ticks between each growth application */
    private static final int GROWTH_TICK_RATE = 1500;

    /** I store the position of the tank supplying my gutter chain */
    private BlockPos supplyingTankPos = null;

    public void setSupplyingTankPos(BlockPos pos) {
        this.supplyingTankPos = pos;
    }

    public BlockPos getSupplyingTankPos() {
        return supplyingTankPos;
    }

    /** I control how many ticks between each tank drain */
    private static final int DRAIN_TICK_RATE = 60;

    /** I count ticks between tank drains */
    private int drainTickCounter = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SprinklerBlockEntity(SprinklerTier tier, BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.SPRINKLER.get(), pos, state);
        this.tier = tier;
    }

    // -------------------------------------------------------------------------
    // Fluid reception — called by GutterBlockEntity above me
    // -------------------------------------------------------------------------

    /**
     * I am called by the GutterBlockEntity directly above me
     * when it has fluid to supply.
     *
     * @param fluidId The fluid being supplied
     */
    public void receiveFluid(String fluidId) {
        this.currentFluidId = fluidId;
        this.receivedFluidThisTick = true;
    }

    /**
     * I return the fluid currently being supplied to me.
     * Empty string means I am dry — no particles should show.
     *
     * @return The current fluid identifier
     */
    public String getCurrentFluidId() {
        return currentFluidId;
    }

    // -------------------------------------------------------------------------
    // Tick logic
    // -------------------------------------------------------------------------

    /**
     * I am called every game tick by SprinklerBlock.
     *
     * If I received fluid this tick I grow plants beneath me.
     * If I did not receive fluid I do nothing — the sprinkler is dry.
     *
     * @param level The server world
     * @param pos   My position
     */

    public void tick(ServerLevel level, BlockPos pos) {
        tickCounter++;

        // Track how long since fluid arrived
        if (receivedFluidThisTick) {
            ticksSinceLastFluid = 0;
        } else {
            ticksSinceLastFluid++;
        }

        // If too long without fluid, go dry
        if (ticksSinceLastFluid > FLUID_TIMEOUT) {
            if (!currentFluidId.isEmpty()) {
                currentFluidId = "";
                setChanged();
                level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
            }
            receivedFluidThisTick = false;
            tickCounter = 0;
            drainTickCounter = 0;
            return;
        }

        // Drain the tank on the drain tick rate — independent of growth
        if (receivedFluidThisTick && !currentFluidId.isEmpty()) {
            drainTickCounter++;
            if (drainTickCounter >= DRAIN_TICK_RATE) {
                drainTickCounter = 0;
                TankBlockEntity tank = null;
                if (supplyingTankPos != null) {
                    if (level.getBlockEntity(supplyingTankPos) instanceof TankBlockEntity t) {
                        tank = t;
                    }
                }
                if (tank != null) {
                    tank.requestFluid(level, 1, currentFluidId);
                }
            }
        }

        // Grow plants on the growth tick rate — independent of drain
        if (receivedFluidThisTick && !currentFluidId.isEmpty() && tickCounter >= GROWTH_TICK_RATE) {
            tickCounter = 0;
            PlantGrowthTicker.tickArea(tier, currentFluidId, level, pos);
            setChanged();
            level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        }

        receivedFluidThisTick = false;
    }

    /**
     * I am called when the gutter above me is broken.
     * I immediately stop receiving fluid.
     */
    public void clearFluid() {
        this.receivedFluidThisTick = false;
        this.currentFluidId = "";
        setChanged();
    }





    // -------------------------------------------------------------------------
    // Client sync — sends block entity data to the client so particles work
    // -------------------------------------------------------------------------

    /**
     * I tell Minecraft to send my data to the client when I am loaded
     * or when setChanged() is called.
     * Without this the client cannot read my fluidId for particle effects.
     */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * I provide the data to send to the client in the update packet.
     * The client uses this to read currentFluidId for particle effects.
     *
     * @param registries The registry lookup provider
     * @return           My NBT data to send to the client
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    /**
     * I save my fluid state so it persists when the chunk unloads.
     *
     * @param tag        The NBT tag to save into
     * @param registries The registry lookup provider
     */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("fluidId", currentFluidId);
    }

    /**
     * I load my fluid state when the chunk reloads.
     *
     * @param tag        The NBT tag to load from
     * @param registries The registry lookup provider
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentFluidId = tag.getString("fluidId");
    }
}