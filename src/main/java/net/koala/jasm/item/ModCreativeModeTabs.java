package net.koala.jasm.item;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModCreativeModeTabs {


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JasMod.MOD_ID);


    public static final Supplier<CreativeModeTab> KCURIOS_ITEMS_TAB = CREATIVE_MODE_TAB.register("jasm_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MOON_BLOCK.get()))
                    .title(Component.translatable("creativetab.justanotherspace.jasm_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.PERIDOT);
                        output.accept(ModItems.OIL_BUCKET.get());


                    }).build());


    public static final Supplier<CreativeModeTab> KCURIOS_BLOCKS_TAB = CREATIVE_MODE_TAB.register("jasm_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MOON_BLOCK.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "jasm_items_tab"))
                    .title(Component.translatable("creativetab.justanotherspace.jasm_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.MOON_BLOCK);
                        output.accept(ModBlocks.PERIDOT_ORE);


                    }).build());


    public static void register(IEventBus eventBus) {

        CREATIVE_MODE_TAB.register(eventBus);
    }
}
