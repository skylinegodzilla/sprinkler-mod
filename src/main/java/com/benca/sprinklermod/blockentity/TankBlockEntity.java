package com.benca.sprinklermod.blockentity;

import com.benca.sprinklermod.growth.GrowthHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
/**
 * I am the memory attached to every tank block.
 *
 * MASTER VS MEMBER BLOCKS:
 *   In a multiblock tank, one block is the master (lowest corner).
 *   The master owns the shared fluid pool.
 *   All other blocks are members — they know who the master is
 *   and delegate fluid operations to it.
 *
 *   If masterPos == my own pos — I am the master.
 *   If masterPos == null — I am not part of a valid structure.
 *   If masterPos == something else — I am a member, delegate to master.
 *
 * HOW FILLING WORKS:
 *   Each tick I check my four horizontal neighbours for flowing water.
 *   If I find any, I add 1 unit to the master's fluid pool every
 *   FILL_RATE ticks.
 *
 * HOW DRAINING WORKS:
 *   GutterBlockEntity calls requestFluid() on me.
 *   If I am a member I forward the request to the master.
 *   If I am the master I check the pool and drain if available.
 *
 * CAPACITY:
 *   Each tank block contributes 64 units to the total pool.
 *   A 2x2 tank holds 256 units total.
 *
 * FILL/DRAIN RATES:
 *   Fill:  1 unit per 500 ticks per flowing water input
 *   Drain: 1 unit per 1500 ticks per sprinkler tick
 *   Therefore 1 water input sustains exactly 3 sprinklers.
 *
 * CODE SMELL WARNING:
 *   String fluid identifier is a temporary placeholder.
 *   Flagged for refactor when FluidRegistry is built.
 */
public class TankBlockEntity extends BlockEntity {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** I fill the pool by this many units per flowing water input per fill tick */
    public static final int FILL_AMOUNT = 1;

    /** I fill every this many ticks per water input */
    public static final int FILL_RATE = 20;

    /** I hold this many units per tank block in the structure */
    public static final int CAPACITY_PER_BLOCK = 64;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * I store the position of the master block.
     * Null means I am not part of a valid structure.
     * Equal to my own pos means I am the master.
     */
    private BlockPos masterPos = null;

    /**
     * I store the fluid pool — only meaningful on the master block.
     * Members always delegate to the master.
     *
     * CODE SMELL: String placeholder — replace with Fluid type
     * when FluidRegistry is built.
     */
    private int fluidUnits = 0;

    /**
     * I store how many tank blocks are in the current structure.
     * Only meaningful on the master block.
     * Used to calculate total capacity.
     */
    private int structureSize = 1;

    /** I count ticks for fill rate throttling */
    private int fillTickCounter = 0;

    public Boolean isEmpty = false;

    public int cooldown = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public TankBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TANK.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Tick logic
    // -------------------------------------------------------------------------

    /**
     * I am called every game tick by TankBlock.
     *
     * I only handle filling the pool from water above me.
     * Gutters pull fluid from me directly by checking if I am above them —
     * I do not push fluid into gutters.
     *
     * @param level The server world
     * @param pos   My position
     */
    public void tick(ServerLevel level, BlockPos pos) {
        if (!isPartOfStructure()) return;
        if (!isMaster()) return;

        fillTickCounter++;
        if (fillTickCounter < FILL_RATE) return;
        fillTickCounter = 0;

        if (fluidUnits <= 1  && isEmpty == false) {
            cooldown = 10;
            isEmpty = true;
        }

        if(cooldown > 0) {
            cooldown --;
            if(cooldown == 0) {
                int totalWaterInputs = countWaterInputsForStructure(level);
                if (totalWaterInputs == 0) return;

                addFluid(totalWaterInputs * FILL_AMOUNT, level);
                addFluid(totalWaterInputs * FILL_AMOUNT, level);
                isEmpty = false;
            }
        } else {
            int totalWaterInputs = countWaterInputsForStructure(level);
            if (totalWaterInputs == 0) return;

            addFluid(totalWaterInputs * FILL_AMOUNT, level);
        }
    }

    // -------------------------------------------------------------------------
    // Structure management — called by TankBlock
    // -------------------------------------------------------------------------

    /**
     * I am called by TankBlock when a valid structure is detected.
     * I store the master position so I know where to delegate fluid ops.
     *
     * @param masterPos The position of the master block
     */
    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
    }

    /**
     * I am called by TankBlock when the structure becomes invalid.
     * I clear my master reference.
     */
    public void clearMasterPos() {
        this.masterPos = null;
        setChanged();
    }

    /**
     * I am called on the master block to update the structure size.
     * This affects total capacity calculation.
     *
     * @param size The number of tank blocks in the structure
     */
    public void setStructureSize(int size) {
        this.structureSize = size;
        setChanged();
    }

    /**
     * I return whether I am the master block of this structure.
     *
     * @return True if I am the master
     */
    public boolean isMaster() {
        return masterPos != null && masterPos.equals(worldPosition);
    }

    /**
     * I return whether I am part of a valid structure.
     *
     * @return True if I have a master (including being the master myself)
     */
    public boolean isPartOfStructure() {
        return masterPos != null;
    }

    /**
     * I invalidate the whole structure when a block is removed.
     * I notify all connected tank entities to clear their master reference.
     *
     * @param level The world
     */
    public void invalidateStructure(ServerLevel level) {
        if (masterPos == null) return;

        // Find all connected tank blocks and clear their master
        com.benca.sprinklermod.tank.TankMultiblockValidator.ValidationResult result =
                com.benca.sprinklermod.tank.TankMultiblockValidator.validate(level, worldPosition);

        if (result.isValid) {
            for (BlockPos pos : result.positions) {
                if (level.getBlockEntity(pos) instanceof TankBlockEntity entity) {
                    entity.clearMasterPos();
                }
            }
        }

        clearMasterPos();
    }

    // -------------------------------------------------------------------------
    // Fluid operations
    // -------------------------------------------------------------------------

    /**
     * I return the total capacity of this structure in fluid units.
     * Only meaningful on the master block.
     *
     * @return Total capacity in units
     */
    public int getTotalCapacity() {
        return structureSize * CAPACITY_PER_BLOCK;
    }

    /**
     * I return how full the tank is as a fraction between 0.0 and 1.0.
     * Used by the client to display fill level.
     *
     * @return Fill fraction
     */
    public float getFillFraction() {
        int capacity = getTotalCapacity();
        if (capacity == 0) return 0;
        return (float) fluidUnits / capacity;
    }

    /**
     * I am called by GutterBlockEntity to request fluid from the tank.
     * If I am a member I forward to the master.
     * If I am the master I drain from the pool if available.
     *
     * @param level    The world
     * @param amount   How many units to drain
     * @param fluidId  The fluid type to provide
     * @return         True if fluid was available and drained
     */
    public boolean requestFluid(ServerLevel level, int amount, String fluidId) {
        // If I am a member, delegate to master
        if (!isMaster() && masterPos != null) {
            if (level.getBlockEntity(masterPos) instanceof TankBlockEntity master) {
                return master.requestFluid(level, amount, fluidId);
            }
            return false;
        }

        // I am the master — check if I have enough fluid
        if (fluidUnits >= amount) {
            fluidUnits -= amount;
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return true;
        }

        return false;
    }

    /**
     * I return the master block position.
     * Null means I am not part of a valid structure.
     *
     * @return The master position or null
     */
    public BlockPos getMasterPos() {
        return masterPos;
    }

    /**
     * I count water inputs for the entire structure by checking above
     * every tank block in the structure.
     * Only called on the master block.
     *
     * @param level The world
     * @return      Total water inputs across all structure blocks
     */
    private int countWaterInputsForStructure(ServerLevel level) {
        com.benca.sprinklermod.tank.TankMultiblockValidator.ValidationResult result =
                com.benca.sprinklermod.tank.TankMultiblockValidator.validate(level, worldPosition);

        if (!result.isValid) return 0;

        int count = 0;
        for (BlockPos structurePos : result.positions) {
            BlockState above = level.getBlockState(structurePos.above());
            if (above.getBlock() == net.minecraft.world.level.block.Blocks.WATER) {
                count++;
            }
        }
        return count;
    }

    /**
     * I add fluid units to the pool up to the total capacity.
     * Only called on the master block.
     *
     * @param amount The number of units to add
     * @param level  The world (for client sync)
     */
    private void addFluid(int amount, ServerLevel level) {
        int capacity = getTotalCapacity();
        fluidUnits = Math.min(fluidUnits + amount, capacity);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /**
     * I count how many adjacent horizontal blocks are flowing water.
     * Each flowing water block counts as one input.
     *
     * @param level The world
     * @param pos   My position
     * @return      Number of flowing water inputs
     */
    private int countAdjacentWaterInputs(ServerLevel level, BlockPos pos) {
        int count = 0;

        // I only accept water from directly above — water pours into the tank
        BlockState above = level.getBlockState(pos.above());
        if (above.getBlock() == net.minecraft.world.level.block.Blocks.WATER) {
            count++;
        }

        return count;
    }

    // -------------------------------------------------------------------------
    // Public getters
    // -------------------------------------------------------------------------

    /**
     * I return the current fluid units in the pool.
     * Only meaningful on the master block.
     *
     * @return Current fluid units
     */
    public int getFluidUnits() {
        return fluidUnits;
    }

    /**
     * I return the current fluid type.
     * Currently always water — will be expanded when FluidRegistry is built.
     *
     * @return The fluid identifier
     */
    public String getFluidId() {
        return fluidUnits > 0 ? GrowthHandler.FLUID_WATER : "";
    }

    // -------------------------------------------------------------------------
    // Client sync
    // -------------------------------------------------------------------------

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // -------------------------------------------------------------------------
    // NBT persistence
    // -------------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("fluidUnits", fluidUnits);
        tag.putInt("structureSize", structureSize);
        if (masterPos != null) {
            tag.putInt("masterX", masterPos.getX());
            tag.putInt("masterY", masterPos.getY());
            tag.putInt("masterZ", masterPos.getZ());
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidUnits = tag.getInt("fluidUnits");
        structureSize = tag.getInt("structureSize");
        if (tag.contains("masterX")) {
            masterPos = new BlockPos(
                    tag.getInt("masterX"),
                    tag.getInt("masterY"),
                    tag.getInt("masterZ")
            );
        }
    }

    /**
     * I feed fluid down into any gutter directly below me.
     * Called after a successful fill tick.
     *
     * @param level The world
     */
    private void feedGuttersBelow(ServerLevel level) {
        for (BlockPos structurePos : getStructurePositions(level)) {
            BlockPos below = structurePos.below();
            if (level.getBlockEntity(below) instanceof GutterBlockEntity gutter) {
                gutter.receiveFluid(GrowthHandler.FLUID_WATER);
            }
        }
    }

    /**
     * I return all positions in this structure by re-validating.
     *
     * @param level The world
     * @return      All structure positions
     */
    private java.util.List<BlockPos> getStructurePositions(ServerLevel level) {
        com.benca.sprinklermod.tank.TankMultiblockValidator.ValidationResult result =
                com.benca.sprinklermod.tank.TankMultiblockValidator.validate(level, worldPosition);
        if (result.isValid) return result.positions;
        return java.util.Collections.emptyList();
    }
}