package net.koala.jasm.datagen;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, JasMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.MOON_BLOCK.get())
                .add(ModBlocks.PERIDOT_ORE.get())
                .add(ModBlocks.FUEL_TANK.get())
                .add(ModBlocks.CONTROL_PANEL.get())
                .add(ModBlocks.BASIC_ENGINE.get())
                .add(ModBlocks.METAL_LADDER.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.CHAIR_BLOCK.get());

        tag(ModTags.Blocks.LAUNCH_PAD)
                .add(ModBlocks.LAUNCH_PAD.get());

        tag(ModTags.Blocks.MOON_NATURALS)
                .add(ModBlocks.MOON_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.FUEL_TANK.get())
                .add(ModBlocks.CONTROL_PANEL.get())
                .add(ModBlocks.BASIC_ENGINE.get())
                .add(ModBlocks.METAL_LADDER.get());
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.MOON_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL);

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.PERIDOT_ORE.get());


    }
}
