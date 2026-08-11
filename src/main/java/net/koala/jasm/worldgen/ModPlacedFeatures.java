package net.koala.jasm.worldgen;

import net.koala.jasm.JasMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class ModPlacedFeatures {

    //where do i place that
    // how many do i place

    public static ResourceKey<PlacedFeature> MOON_PERIDOT_PLACED_KEY = registerKey("peridot_ore_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        //define PF
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        register(context, MOON_PERIDOT_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.MOON_PERIDOT_ORE_KEY),
                ModOrePlacement.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(20))));


    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?,?>> configuration,
                                 List<PlacementModifier> modifiers)
    {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }






}
