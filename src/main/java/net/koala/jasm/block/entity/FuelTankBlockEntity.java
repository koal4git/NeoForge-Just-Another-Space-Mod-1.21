package net.koala.jasm.block.entity;

import net.koala.jasm.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FuelTankBlockEntity extends BlockEntity {

    private int currentFuel = 0;

    public FuelTankBlockEntity( BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FUEL_TANK.get(), pos, blockState);
    }

    public int getCurrentFuel() {
        return currentFuel;
    }

    public void setCurrentFuel(int amount) {
        this.currentFuel = Math.max(0 ,amount);
        setChanged();
    }


    //returns amounbt drained
    public int drain(int amount) {
        int drained = Math.min(currentFuel, amount);
        setCurrentFuel(currentFuel - drained);
        return drained;
    }


    //return amount added
    public int fill(int amount, int capacity) {
        int space = capacity - currentFuel;
        int filled = Math.min(space, amount);
        setCurrentFuel(currentFuel + filled);
        return filled;
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CurrentFuel", currentFuel);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        currentFuel = tag.getInt("CurrentFuel");
    }
}
