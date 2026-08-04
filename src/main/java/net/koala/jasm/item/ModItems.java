package net.koala.jasm.item;

import net.koala.jasm.JasMod;
import net.koala.jasm.fluid.ModFluids;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JasMod.MOD_ID);

    public static final DeferredItem<Item> PERIDOT = ITEMS.register("peridot",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, BucketItem> OIL_BUCKET = ITEMS.register("oil_bucket",
            () -> new BucketItem(ModFluids.OIL_SOURCE.get(), new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}