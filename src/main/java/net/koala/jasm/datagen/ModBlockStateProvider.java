package net.koala.jasm.datagen;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.LadderBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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

        //chair
        ModelFile chairModel = models().getExistingFile(modLoc("block/chair_block"));
        horizontalBlock(ModBlocks.CHAIR_BLOCK.get(), chairModel);
        simpleBlockItem(ModBlocks.CHAIR_BLOCK.get(), chairModel);

        ModelFile laddermodel = models().withExistingParent("space_ladder", mcLoc("block/ladder"))
                .texture("texture", modLoc("block/metal_ladder"));

        getVariantBuilder(ModBlocks.METAL_LADDER.get()).forAllStates(state -> {
                    Direction facing = state.getValue(LadderBlock.FACING);
                    return ConfiguredModel.builder().modelFile(laddermodel)
                            .rotationY((int) facing.toYRot())
                            .build();});
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
