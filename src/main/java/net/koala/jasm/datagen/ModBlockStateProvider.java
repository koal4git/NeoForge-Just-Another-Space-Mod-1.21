package net.koala.jasm.datagen;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {


    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, JasMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        //blockWithItem(ModBlocks.MAGIC_BLOCK);
        blockWithItem(ModBlocks.MOON_BLOCK);
        blockWithItem(ModBlocks.PERIDOT_ORE);

        blockWithItem(ModBlocks.CONTROL_PANEL);
        blockWithItem(ModBlocks.BASIC_ENGINE);
        blockWithItem(ModBlocks.FUEL_TANK);
        blockWithItem(ModBlocks.LAUNCH_PAD);

    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("JasMod:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix){
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("JasMod:block/" + deferredBlock.getId().getPath() + appendix));
    }

}
