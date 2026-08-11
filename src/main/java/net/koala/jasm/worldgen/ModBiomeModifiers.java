package net.koala.jasm.worldgen;

import net.koala.jasm.JasMod;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {
    //what biome and what stage will i place this in


    public static final ResourceKey<BiomeModifier> ADD_PERIDOT_ORE = registerKey("add_peridot_ore");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        ResourceKey<Biome> MOON_BIOME = ResourceKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(
                        JasMod.MOD_ID,
                        "moon"
                )
        );

        context.register(ADD_PERIDOT_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(MOON_BIOME)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.MOON_PERIDOT_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));


    }

    public static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, name));
    }



}
