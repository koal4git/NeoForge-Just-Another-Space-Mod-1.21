package net.koala.jasm.datagen;

import net.koala.jasm.JasMod;
import net.koala.jasm.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.LinkedHashMap;


public class ModItemModelProvider extends ItemModelProvider {

    private static LinkedHashMap<ResourceKey<TrimMaterial>, Float> trimMaterials = new LinkedHashMap<>();
    static {
        trimMaterials.put(TrimMaterials.QUARTZ, 0.1f);
        trimMaterials.put(TrimMaterials.IRON, 0.2f);
        trimMaterials.put(TrimMaterials.NETHERITE, 0.3f);
        trimMaterials.put(TrimMaterials.REDSTONE, 0.4f);
        trimMaterials.put(TrimMaterials.COPPER, 0.5f);
        trimMaterials.put(TrimMaterials.GOLD, 0.6f);
        trimMaterials.put(TrimMaterials.EMERALD, 0.7f);
        trimMaterials.put(TrimMaterials.DIAMOND, 0.8f);
        trimMaterials.put(TrimMaterials.LAPIS, 0.9f);
        trimMaterials.put(TrimMaterials.AMETHYST, 1.0f);
    }




    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, JasMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        //basicItem(ModItems.STEEL_INGOT.get());
        //handheldItem(ModItems.AMETHYST_HAMMER);
        basicItem(ModItems.PERIDOT.get());
        basicItem(ModItems.OIL_BUCKET.get());

    }


    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/" + item.getId().getPath()));

    }


    private void bowItem(DeferredItem<Item> item) {

        // Bow Item
        this.withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/" + item.getId().getPath()))

                //transforms by luisscalabrin579 on YT
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(-40, 260, -40) // was -80
                .translation(-1, -2, 2.5f)
                .scale(0.9f)
                .end()

                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(-40, -280, 40) // was -80
                .translation(-1, -2, 2.5f)
                .scale(0.9f)
                .end()

                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(35, -90, 15) // was 0
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f)
                .end()

                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(35, 90, -15) // was 0
                .translation(1.13f, 3.2f, 1.13f)
                .scale(0.68f)
                .end()
                .end()



                .override()
                .predicate(mcLoc("pulling"), 1f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + item.getId().getPath() + "_pulling_0")))
                .end()


                .override()
                .predicate(mcLoc("pulling"), 1f)
                .predicate(mcLoc("pull"), 0.65f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + item.getId().getPath() + "_pulling_1")))
                .end()

                .override()
                .predicate(mcLoc("pulling"), 1f)
                .predicate(mcLoc("pull"), 0.9f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/" + item.getId().getPath() + "_pulling_2")))
                .end();


        // bow pulling 0
        this.withExistingParent(item.getId().getPath() + "_pulling_0", modLoc("item/" + item.getId().getPath()))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/" + item.getId().getPath() + "_pulling_0"));

        // bow pulling 1
        this.withExistingParent(item.getId().getPath() + "_pulling_1", modLoc("item/" + item.getId().getPath()))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/" + item.getId().getPath() + "_pulling_1"));

        // bow pulling 2
        this.withExistingParent(item.getId().getPath() + "_pulling_2", modLoc("item/" + item.getId().getPath()))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/" + item.getId().getPath() + "_pulling_2"));
    }

    private void chiselItem(DeferredItem<Item> item) {

        // Chisel Item
        this.withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/chisel"))
                .override()
                .model(new ModelFile.UncheckedModelFile(modLoc("item/chisel_used")))
                .predicate(modLoc("used"), 1f)
                .end();

        // Chisel Used
        this.withExistingParent("chisel_used", mcLoc("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID, "item/chisel_used"));
    }

    // Credit to El_Redstoniano for making this and kaupenjoe for tweaking for 1.21
    private void trimmedArmorItem(DeferredItem<ArmorItem> itemDeferredItem) {
        final String MOD_ID = JasMod.MOD_ID; // Change this to your mod id

        if(itemDeferredItem.get() instanceof ArmorItem armorItem) {
            trimMaterials.forEach((trimMaterial, value) -> {
                float trimValue = value;

                String armorType = switch (armorItem.getEquipmentSlot()) {
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> "";
                };

                String armorItemPath = armorItem.toString();
                String trimPath = "trims/items/" + armorType + "_trim_" + trimMaterial.location().getPath();
                String currentTrimName = armorItemPath + "_" + trimMaterial.location().getPath() + "_trim";
                ResourceLocation armorItemResLoc = ResourceLocation.parse(armorItemPath);
                ResourceLocation trimResLoc = ResourceLocation.parse(trimPath); // minecraft namespace
                ResourceLocation trimNameResLoc = ResourceLocation.parse(currentTrimName);

                // This is used for making the ExistingFileHelper acknowledge that this texture exist, so this will
                // avoid an IllegalArgumentException
                existingFileHelper.trackGenerated(trimResLoc, PackType.CLIENT_RESOURCES, ".png", "textures");

                // Trimmed armorItem files
                getBuilder(currentTrimName)
                        .parent(new ModelFile.UncheckedModelFile("item/generated"))
                        .texture("layer0", armorItemResLoc.getNamespace() + ":item/" + armorItemResLoc.getPath())
                        .texture("layer1", trimResLoc);

                // Non-trimmed armorItem file (normal variant)
                this.withExistingParent(itemDeferredItem.getId().getPath(),
                                mcLoc("item/generated"))
                        .override()
                        .model(new ModelFile.UncheckedModelFile(trimNameResLoc.getNamespace()  + ":item/" + trimNameResLoc.getPath()))
                        .predicate(mcLoc("trim_type"), trimValue).end()
                        .texture("layer0",
                                ResourceLocation.fromNamespaceAndPath(MOD_ID,
                                        "item/" + itemDeferredItem.getId().getPath()));
            });
        }
    }


    public void buttonItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID
                        , "block/" + baseBlock.getId().getPath()));
    }

    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID
                        , "block/" + baseBlock.getId().getPath()));
    }

    public void wallItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall", ResourceLocation.fromNamespaceAndPath(JasMod.MOD_ID
                        , "block/" + baseBlock.getId().getPath()));
    }








}
