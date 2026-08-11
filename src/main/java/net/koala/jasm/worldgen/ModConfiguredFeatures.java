package net.koala.jasm.worldgen;

import net.koala.jasm.JasMod;
import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.util.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    //CF -> PF -> BM
    //what tree look like
    //how many ore blocks
    //how will ip lace flowers

    public static final ResourceKey<ConfiguredFeature<?,?>> MOON_PERIDOT_ORE_KEY = registerKey("moon_peridot_ore");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?,?>> context) {

        //define CF here

        RuleTest moonblockReplaceables = new TagMatchTest(ModTags.Blocks.MOON_NATURALS);

        List<OreConfiguration.TargetBlockState> moonPeridot = List.of(

                OreConfiguration.target(moonblockReplaceables, ModBlocks.PERIDOT_ORE.get().defaultBlockState())
        );

        register(context, MOON_PERIDOT_ORE_KEY, Feature.ORE, new OreConfiguration(moonPeridot, 7));


    }


    public static ResourceKey<ConfiguredFeature<?,?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?,?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?,?>> key, F feature, FC configuration)
    {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }



}
