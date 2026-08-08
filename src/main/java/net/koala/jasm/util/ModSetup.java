package net.koala.jasm.util;

import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.component.ModComponents;

public class ModSetup {


    public static void init() {
        ModComponents.register(ModBlocks.CONTROL_PANEL.get(), (s, be) -> ModBlocks.CONTROL_PANEL.get());
        ModComponents.register(ModBlocks.FUEL_TANK.get(), (s, be) -> ModBlocks.FUEL_TANK.get());
        ModComponents.register(ModBlocks.BASIC_ENGINE.get(), (s, be) -> ModBlocks.BASIC_ENGINE.get());
    }
}
