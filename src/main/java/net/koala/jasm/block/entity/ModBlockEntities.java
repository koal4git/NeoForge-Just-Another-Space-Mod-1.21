package net.koala.jasm.block.entity;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, JasMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ControlPanelBlockEntity>> CONTROL_PANEL =
            BLOCK_ENTITIES.register("control_panel", () -> BlockEntityType.Builder.of(
                    ControlPanelBlockEntity::new, ModBlocks.CONTROL_PANEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FuelTankBlockEntity>> FUEL_TANK =
            BLOCK_ENTITIES.register("fuel_tank", () -> BlockEntityType.Builder.of(
                    FuelTankBlockEntity::new, ModBlocks.FUEL_TANK.get()).build(null));

    private ModBlockEntities() {}

    /** Call once from your main mod class constructor: ModBlockEntities.register(modEventBus); */
    public static void register(net.neoforged.bus.api.IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}