package net.koala.jasm.util;

import net.koala.jasm.JasMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.swing.text.html.HTML;

public class ModTags {

    public static class Blocks {

        public static final TagKey<Block> LAUNCH_PAD = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "launch_pad"));

        public static final TagKey<Block> MOON_NATURALS = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "moon_naturals"));


        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, name));
        }
    }

    public static class Items {

        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");



        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, name));
        }
    }
}