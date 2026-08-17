package net.koala.jasm.component;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

public interface FuelTankComponent extends SpacecraftComponent {

    /**
     * Checks whether this tank can accept the supplied fuel.
     */
    boolean isValidFuel(FluidStack stack);

    /**
     * Maximum capacity of this tank in mB.
     */
    int getCapacity();

    /**
     * Current amount of fuel in the tank.
     */
    int getCurrentFuel(BlockEntity blockEntity);

    /**
     * Returns the fuel currently stored.
     */
    FluidStack getFuel(BlockEntity blockEntity);
}