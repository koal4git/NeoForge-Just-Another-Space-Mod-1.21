package net.koala.jasm.block;

import net.koala.jasm.JasMod;
import net.koala.jasm.fluid.ModFluids;
import net.koala.jasm.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JasMod.MOD_ID);

    public static final DeferredBlock<Block> MOON_BLOCK = registerBlock("moon_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final DeferredBlock<Block> PERIDOT_ORE = registerBlock("peridot_ore",
            () -> new Block(BlockBehaviour.Properties.of().strength(1.5f).requiresCorrectToolForDrops().sound(SoundType.STONE)));

    public static final DeferredHolder<Block, LiquidBlock> OIL_BLOCK = BLOCKS.register("oil_fluid",
            () -> new LiquidBlock(ModFluids.OIL_SOURCE.get(), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .replaceable()
                    .noCollission()
                    .strength(100f)
                    .noLootTable()
                    .liquid()
                    .sound(SoundType.EMPTY)
                    .pushReaction(PushReaction.IGNORE)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}