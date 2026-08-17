package net.koala.jasm.block.entity;

import net.koala.jasm.block.custom.FuelTankBlock;
import net.koala.jasm.component.FuelTankComponent;
import net.koala.jasm.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlockEntity extends BlockEntity {

    // 4 buckets per tank
    public static final int CAPACITY_PER_TANK = 4000;

    // Number of visual levels
    public static final int LEVEL_STEPS = 6;

    private final FluidTank tank = new FluidTank(CAPACITY_PER_TANK) {

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty()
                    && getBlockState().getBlock() instanceof FuelTankComponent fuelTank
                    && fuelTank.isValidFuel(stack);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
            updateBlockStateLevel();

            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(
                        worldPosition,
                        getBlockState(),
                        getBlockState(),
                        3
                );
            }
        }
    };

    public FuelTankBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FUEL_TANK.get(), pos, blockState);
    }

    /*
     * The actual fluid handler.
     */
    public IFluidHandler getFluidHandler() {
        return tank;
    }

    public int getCurrentFuel() {
        return tank.getFluidAmount();
    }

    public int getCapacity() {
        return tank.getCapacity();
    }

    public FluidStack getFluid() {
        return tank.getFluid();
    }

    /*
     * Returns amount successfully drained.
     */
    public int drain(int amount) {
        return tank
                .drain(amount, IFluidHandler.FluidAction.EXECUTE)
                .getAmount();
    }

    /*
     * Returns amount successfully filled.
     */
    public int fill(FluidStack stack) {
        return tank.fill(
                stack,
                IFluidHandler.FluidAction.EXECUTE
        );
    }

    /*
     * Visual fluid level from 0 -> LEVEL_STEPS.
     */
    public int getRenderLevel() {

        if (tank.getCapacity() <= 0 || tank.getFluidAmount() <= 0) {
            return 0;
        }

        int level = Math.round(
                (float) tank.getFluidAmount()
                        / tank.getCapacity()
                        * LEVEL_STEPS
        );

        return Math.max(
                1,
                Math.min(LEVEL_STEPS, level)
        );
    }

    /*
     * Updates the blockstate used by the tank model.
     */
    private void updateBlockStateLevel() {

        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        BlockState state = this.getBlockState();

        if (state.getBlock() instanceof FuelTankBlock
                && state.hasProperty(FuelTankBlock.LEVEL)) {

            int newLevel = getRenderLevel();

            if (state.getValue(FuelTankBlock.LEVEL) != newLevel) {

                this.level.setBlock(
                        this.worldPosition,
                        state.setValue(
                                FuelTankBlock.LEVEL,
                                newLevel
                        ),
                        3
                );
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        updateBlockStateLevel();
    }

    /*
     * Save tank contents.
     */
    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        tag.put(
                "Fuel",
                tank.writeToNBT(
                        registries,
                        new CompoundTag()
                )
        );
    }

    /*
     * Load tank contents.
     */
    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        if (tag.contains("Fuel")) {
            tank.readFromNBT(
                    registries,
                    tag.getCompound("Fuel")
            );
        }

        updateBlockStateLevel();
    }

    /*
     * Sync tank contents to client.
     */
    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries) {

        CompoundTag tag = super.getUpdateTag(registries);

        tag.put(
                "Fuel",
                tank.writeToNBT(
                        registries,
                        new CompoundTag()
                )
        );

        return tag;
    }

    @Override
    public void handleUpdateTag(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        super.handleUpdateTag(tag, registries);

        if (tag.contains("Fuel")) {
            tank.readFromNBT(
                    registries,
                    tag.getCompound("Fuel")
            );
        }

        updateBlockStateLevel();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /*
     * Reads the saved fuel amount without creating
     * the actual block entity.
     */
    public static int readSavedFuelAmount(
            @Nullable CompoundTag savedData,
            HolderLookup.Provider registries) {

        if (savedData == null || !savedData.contains("Fuel")) {
            return 0;
        }

        FluidTank scratch =
                new FluidTank(Integer.MAX_VALUE);

        scratch.readFromNBT(
                registries,
                savedData.getCompound("Fuel")
        );

        return scratch.getFluidAmount();
    }
}