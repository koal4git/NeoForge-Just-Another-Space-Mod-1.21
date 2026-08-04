package net.koala.jasm.fluid;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.item.ModItems;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import net.minecraft.core.registries.Registries;

public class ModFluids {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, JasMod.MOD_ID);

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> OIL_SOURCE = FLUIDS.register("oil_fluid",
            () -> new BaseFlowingFluid.Source(oilProperties()));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> OIL_FLOWING = FLUIDS.register("flowing_oil",
            () -> new BaseFlowingFluid.Flowing(oilProperties()));

    private static BaseFlowingFluid.Properties oilProperties() {
        return new BaseFlowingFluid.Properties(ModFluidTypes.OIL_FLUID_TYPE, OIL_SOURCE, OIL_FLOWING)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .block(ModBlocks.OIL_BLOCK)
                .bucket(ModItems.OIL_BUCKET);
    }

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}