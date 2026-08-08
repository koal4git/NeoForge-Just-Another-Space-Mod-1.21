package net.koala.jasm.component;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface FuelTankComponent {


     String getFuelType();

     int getCapacity();


    //current fuel of tanks blockentity
     int getCurrentFuel(BlockEntity blockEntity);

}
