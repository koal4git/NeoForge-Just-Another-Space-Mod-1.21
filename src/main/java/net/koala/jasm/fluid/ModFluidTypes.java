package net.koala.jasm.fluid;

import net.koala.jasm.JasMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL = ResourceLocation.withDefaultNamespace("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.withDefaultNamespace("block/water_flow");
    public static final ResourceLocation OIL_OVERLAY_RL = ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "misc/in_oil");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, JasMod.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> OIL_FLUID_TYPE = FLUID_TYPES.register("oil_fluid",
            () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, OIL_OVERLAY_RL,
                    0xFF1A1208, // dark brownish-black tint
                    new Vector3f(20f / 255f, 15f / 255f, 8f / 255f),
                    FluidType.Properties.create()
                            .lightLevel(0)
                            .density(1100)   // heavier than water
                            .viscosity(6000) // thick/slow like real oil
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}