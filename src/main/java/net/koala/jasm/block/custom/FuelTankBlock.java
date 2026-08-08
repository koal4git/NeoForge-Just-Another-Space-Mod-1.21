package net.koala.jasm.block.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.koala.jasm.block.entity.FuelTankBlockEntity;
import net.koala.jasm.component.FuelTankComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends BaseEntityBlock implements FuelTankComponent {

    public static final MapCodec<FuelTankBlock> CODEC = simpleCodec(FuelTankBlock::new);

    private final int capacity;
    private final String fuelType;

    public FuelTankBlock(Properties properties) {

        //placeholder data until tank tiers exist
        this(properties, 12000, "rocket_fuel");
    }

    public FuelTankBlock(Properties properties, int capacity, String fuelType) {
        super(properties);
        this.capacity = capacity;
        this.fuelType = fuelType;
    }



    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuelTankBlockEntity(pos, state);
    }


    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public String getFuelType() {
        return fuelType;
    }
    @Override
    public int getCapacity() {
        return capacity;
    }
    @Override
    public int getCurrentFuel(BlockEntity blockEntity) {
        if (blockEntity instanceof FuelTankBlockEntity tank) {
            return tank.getCurrentFuel();
        }
        return 0;
    }

}
