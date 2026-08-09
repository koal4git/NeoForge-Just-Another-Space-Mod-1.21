package net.koala.jasm.datagen;

import net.koala.jasm.block.ModBlocks;
import net.koala.jasm.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;


public class ModBlockLootTableProvider extends BlockLootSubProvider {


    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(),registries);
    }

    @Override
    protected void generate() {

        //dropSelf(ModBlocks.CRUSHED_EMERALD_BLOCK.get());
        dropSelf(ModBlocks.MOON_BLOCK.get());
        add(ModBlocks.PERIDOT_ORE.get(), createMultipleOreDrops(ModBlocks.PERIDOT_ORE.get(), ModItems.PERIDOT.get(), 1, 3));

        dropSelf(ModBlocks.BASIC_ENGINE.get());
        dropSelf(ModBlocks.FUEL_TANK.get());
        dropSelf(ModBlocks.CONTROL_PANEL.get());
        dropSelf(ModBlocks.LAUNCH_PAD.get());
        dropSelf(ModBlocks.CHAIR_BLOCK.get());

        dropSelf(ModBlocks.METAL_LADDER.get());


    }
    //this is the copper ore drop method but changed
    protected LootTable.Builder createMultipleOreDrops(Block block, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable( block,
                this.applyExplosionDecay( block, LootItem.lootTableItem(item) // changed from items.raw_copper to item to make it multiuse
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))  //from 2f anf 5f to min and max drops
                        .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))));
    }


    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(Holder::value)
                .filter(block -> block != ModBlocks.OIL_BLOCK.get())
                ::iterator;
        //return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
