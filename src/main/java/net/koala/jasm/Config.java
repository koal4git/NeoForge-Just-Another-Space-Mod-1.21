package net.koala.jasm;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_ROCKET_BLOCKS = BUILDER
            .comment("max amount of block you can use toi build a rocket")
            .defineInRange("maxRocketBlocks", -1, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_ROCKET_WIDTH = BUILDER
            .comment("max amount of block you can use toi build a rocket")
            .defineInRange("maxRocketWidth", -1, -1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_ROCKET_HEIGHT = BUILDER
            .comment("max height of rocket")
            .defineInRange("maxRocketHeight", -1, -1, Integer.MAX_VALUE);


    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
