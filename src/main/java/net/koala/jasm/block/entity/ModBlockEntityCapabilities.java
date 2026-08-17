package net.koala.jasm.block.entity;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ModBlockEntityCapabilities {

    public static void registerCapabilities(IEventBus eventBus) {
        eventBus.addListener(
                ModBlockEntityCapabilities::register
        );
    }

    private static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.FUEL_TANK.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );
    }
}