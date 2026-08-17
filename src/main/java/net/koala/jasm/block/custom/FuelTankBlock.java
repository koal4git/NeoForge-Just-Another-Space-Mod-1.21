package net.koala.jasm.block.custom;

import com.mojang.serialization.MapCodec;
import net.koala.jasm.block.entity.FuelTankBlockEntity;
import net.koala.jasm.component.FuelTankComponent;
import net.koala.jasm.fluid.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FuelTankBlock extends BaseEntityBlock implements FuelTankComponent {

    public static final MapCodec<FuelTankBlock> CODEC =
            simpleCodec(FuelTankBlock::new);

    public static final IntegerProperty LEVEL =
            IntegerProperty.create(
                    "level",
                    0,
                    FuelTankBlockEntity.LEVEL_STEPS
            );

    public FuelTankBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(LEVEL, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder) {

        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state) {

        return new FuelTankBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Handles buckets and other fluid containers.
     */
    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            net.minecraft.world.entity.player.Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {

        /*
         * Try to interact with the tank's fluid capability.
         *
         * This handles:
         * - Oil bucket -> tank
         * - Tank -> bucket
         * - Empty bucket
         * - Other compatible fluid containers
         */
        if (FluidUtil.interactWithFluidHandler(
                player,
                hand,
                level,
                pos,
                hitResult.getDirection())) {

            return ItemInteractionResult.SUCCESS;
        }

        /*
         * Important:
         *
         * Returning PASS_TO_DEFAULT_BLOCK_INTERACTION allows
         * normal Minecraft interaction to continue if the item
         * wasn't a valid fluid interaction.
         */
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Which fluids this tank accepts.
     *
     * Add more fluids here later.
     */
    @Override
    public boolean isValidFuel(FluidStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.getFluid() == ModFluids.OIL_SOURCE.get();

        /*
         * Future:
         *
         * return stack.getFluid() == ModFluids.OIL_SOURCE.get()
         *         || stack.getFluid() == ModFluids.ROCKET_FUEL_SOURCE.get()
         *         || stack.getFluid() == ModFluids.HYDROGEN_SOURCE.get();
         */
    }

    @Override
    public int getCapacity() {
        return FuelTankBlockEntity.CAPACITY_PER_TANK;
    }

    @Override
    public int getCurrentFuel(BlockEntity blockEntity) {

        if (blockEntity instanceof FuelTankBlockEntity tank) {
            return tank.getCurrentFuel();
        }

        return 0;
    }

    @Override
    public FluidStack getFuel(BlockEntity blockEntity) {

        if (blockEntity instanceof FuelTankBlockEntity tank) {
            return tank.getFluid();
        }

        return FluidStack.EMPTY;
    }
}