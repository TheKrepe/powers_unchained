package net.threetag.palladium.datagen.internal;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.flag.PalladiumFeatureFlags;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.recipe.ExtrapolatorCloningRecipe;
import net.threetag.palladium.item.recipe.ExtrapolatorTransferRecipe;
import net.threetag.palladium.item.recipe.MultiversalVariantsRecipe;
import net.threetag.palladium.tag.PalladiumItemTags;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PalladiumRecipeProvider extends RecipeProvider {

    public static final ImmutableList<ItemLike> LEAD_SMELTABLES = ImmutableList.of(PalladiumItems.RAW_LEAD, PalladiumItems.LEAD_ORE, PalladiumItems.DEEPSLATE_LEAD_ORE, PalladiumItems.METEORITE_LEAD_ORE);
    public static final ImmutableList<ItemLike> TITANIUM_SMELTABLES = ImmutableList.of(PalladiumItems.RAW_TITANIUM, PalladiumItems.TITANIUM_ORE, PalladiumItems.DEEPSLATE_TITANIUM_ORE, PalladiumItems.METEORITE_TITANIUM_ORE);
    public static final ImmutableList<ItemLike> VIBRANIUM_SMELTABLES = ImmutableList.of(PalladiumItems.RAW_VIBRANIUM, PalladiumItems.VIBRANIUM_ORE, PalladiumItems.DEEPSLATE_VIBRANIUM_ORE, PalladiumItems.METEORITE_VIBRANIUM_ORE);

    public PalladiumRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        var leadEnabled = this.output.withConditions(PalladiumFeatureFlags.conditionLead());
        var titaniumEnabled = this.output.withConditions(PalladiumFeatureFlags.conditionTitanium());
        var vibraniumEnabled = this.output.withConditions(PalladiumFeatureFlags.conditionVibranium());

        this.output.withConditions(PalladiumFeatureFlags.conditionMultiverse())
                .accept(ResourceKey.create(Registries.RECIPE, Palladium.id("multiversal_variants")), MultiversalVariantsRecipe.INSTANCE, null);

        var extrapolatorCrafting = this.output.withConditions(PalladiumFeatureFlags.conditionMultiverse(), PalladiumFeatureFlags.conditionCircuits());
        extrapolatorCrafting.accept(ResourceKey.create(Registries.RECIPE, Palladium.id("multiversal_extrapolator_transfer")),
                new ExtrapolatorTransferRecipe(List.of(SizedIngredient.of(PalladiumItems.QUARTZ_CIRCUIT, 1))), null);
        this.output.withConditions(PalladiumFeatureFlags.conditionMultiverse(), PalladiumFeatureFlags.conditionCircuits(), PalladiumFeatureFlags.conditionVibranium())
                .accept(ResourceKey.create(Registries.RECIPE, Palladium.id("multiversal_extrapolator_cloning")),
                        new ExtrapolatorCloningRecipe(List.of(SizedIngredient.of(PalladiumItems.VIBRANIUM_CIRCUIT, 1), SizedIngredient.of(Items.DIAMOND, 7))), null);
        this.shapeless(RecipeCategory.MISC, PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get())
                .requires(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get())
                .requires(PalladiumItems.QUARTZ_CIRCUIT.get())
                .unlockedBy(getHasName(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get()), has(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get()))
                .save(extrapolatorCrafting, Palladium.id("multiversal_extrapolator_reset").toString());

        this.shaped(RecipeCategory.MISC, PalladiumItems.VIAL.get(), 3)
                .pattern("GIG")
                .pattern("G G")
                .pattern("GIG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy(getHasName(Items.GLASS), has(Tags.Items.GLASS_BLOCKS))
                .save(this.output.withConditions(PalladiumFeatureFlags.conditionVials()));

        this.shapeless(RecipeCategory.MISC, Items.PURPLE_DYE)
                .requires(PalladiumItems.HEART_SHAPED_HERB.get())
                .group("purple_dye")
                .unlockedBy(getHasName(PalladiumItems.HEART_SHAPED_HERB.get()), has(PalladiumItems.HEART_SHAPED_HERB.get()))
                .save(this.output.withConditions(PalladiumFeatureFlags.conditionHeartShapedHerb()), Palladium.id("purple_dye_from_heart_shaped_herb").toString());

        // Circuits
        this.shaped(RecipeCategory.MISC, PalladiumItems.LEAD_CIRCUIT.get())
                .pattern("II")
                .pattern("LL")
                .pattern("GG")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('L', PalladiumItemTags.LEAD_INGOTS)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy(getHasName(PalladiumItems.LEAD_INGOT.get()), has(PalladiumItemTags.LEAD_INGOTS))
                .save(this.output.withConditions(PalladiumFeatureFlags.conditionCircuits(), PalladiumFeatureFlags.conditionLead()));
        this.shaped(RecipeCategory.MISC, PalladiumItems.QUARTZ_CIRCUIT.get())
                .pattern("II")
                .pattern("QQ")
                .pattern("CC")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('Q', Tags.Items.GEMS_QUARTZ)
                .define('C', Tags.Items.INGOTS_COPPER)
                .unlockedBy(getHasName(Items.QUARTZ), has(Tags.Items.GEMS_QUARTZ))
                .save(this.output.withConditions(PalladiumFeatureFlags.conditionCircuits()));
        this.shaped(RecipeCategory.MISC, PalladiumItems.VIBRANIUM_CIRCUIT.get())
                .pattern("II")
                .pattern("VV")
                .pattern("DD")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('V', PalladiumItemTags.VIBRANIUM_INGOTS)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy(getHasName(PalladiumItems.VIBRANIUM_INGOT.get()), has(PalladiumItemTags.VIBRANIUM_INGOTS))
                .save(this.output.withConditions(PalladiumFeatureFlags.conditionCircuits(), PalladiumFeatureFlags.conditionVibranium()));

        this.shaped(RecipeCategory.DECORATIONS, PalladiumItems.SUIT_STAND.get()).pattern(" B ").pattern("SBS").pattern("SXS").define('B', Tags.Items.GEMS_QUARTZ).define('S', Ingredient.of(Blocks.QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ_SLAB)).define('X', Blocks.SMOOTH_STONE_SLAB).unlockedBy(getHasName(Items.ARMOR_STAND), has(Items.ARMOR_STAND)).save(this.output);
        this.shaped(RecipeCategory.DECORATIONS, PalladiumItems.TAILORING_BENCH.get()).pattern("SF").pattern("WW").define('S', Tags.Items.TOOLS_SHEAR).define('F', PalladiumItemTags.FABRICS).define('W', ItemTags.PLANKS).unlockedBy("unlock_right_away", PlayerTrigger.TriggerInstance.tick()).showNotification(false).save(this.output.withConditions(PalladiumFeatureFlags.conditionTailoring()));

        // Materials
        this.shaped(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICKS, 4)
                .define('S', PalladiumBlocks.METEORITE_STONE)
                .pattern("SS")
                .pattern("SS")
                .unlockedBy(getHasName(PalladiumBlocks.METEORITE_STONE), this.has(PalladiumBlocks.METEORITE_STONE))
                .save(this.output);
        this.stairBuilder(PalladiumItems.METEORITE_STONE_STAIRS, Ingredient.of(PalladiumItems.METEORITE_STONE))
                .unlockedBy(getHasName(PalladiumItems.METEORITE_STONE), this.has(PalladiumItems.METEORITE_STONE))
                .save(this.output);
        this.slab(RecipeCategory.BUILDING_BLOCKS, PalladiumItems.METEORITE_STONE_SLAB, PalladiumItems.METEORITE_STONE);
        this.wall(RecipeCategory.BUILDING_BLOCKS, PalladiumItems.METEORITE_STONE_WALL, PalladiumItems.METEORITE_STONE);
        this.stairBuilder(PalladiumItems.METEORITE_BRICK_STAIRS, Ingredient.of(PalladiumItems.METEORITE_BRICKS))
                .unlockedBy(getHasName(PalladiumItems.METEORITE_BRICKS), this.has(PalladiumItems.METEORITE_BRICKS))
                .save(this.output);
        this.slab(RecipeCategory.BUILDING_BLOCKS, PalladiumItems.METEORITE_BRICK_SLAB, PalladiumItems.METEORITE_BRICKS);
        this.wall(RecipeCategory.BUILDING_BLOCKS, PalladiumItems.METEORITE_BRICK_WALL, PalladiumItems.METEORITE_BRICKS);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_STONE_STAIRS, PalladiumBlocks.METEORITE_STONE);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_STONE_WALL, PalladiumBlocks.METEORITE_STONE);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_STONE_SLAB, PalladiumBlocks.METEORITE_STONE, 2);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICKS, PalladiumBlocks.METEORITE_STONE);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_STAIRS, PalladiumBlocks.METEORITE_STONE);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_WALL, PalladiumBlocks.METEORITE_STONE);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_SLAB, PalladiumBlocks.METEORITE_STONE, 2);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_STAIRS, PalladiumBlocks.METEORITE_BRICKS);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_WALL, PalladiumBlocks.METEORITE_BRICKS);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, PalladiumBlocks.METEORITE_BRICK_SLAB, PalladiumBlocks.METEORITE_BRICKS, 2);

        // Ores
        this.oreProcessing(LEAD_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, PalladiumItems.LEAD_INGOT, 0.7F, 200, "lead_ingot", leadEnabled);
        this.nineBlockStorageRecipes(RecipeCategory.MISC, PalladiumItems.RAW_LEAD, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.RAW_LEAD_BLOCK, leadEnabled);
        this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
                RecipeCategory.MISC, PalladiumItems.LEAD_INGOT, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.LEAD_BLOCK, Palladium.id("lead_ingot_from_lead_block").toString(), "lead_ingot", leadEnabled
        );
        this.nineBlockStorageRecipesWithCustomPacking(
                RecipeCategory.MISC, PalladiumItems.LEAD_NUGGET, RecipeCategory.MISC, PalladiumItems.LEAD_INGOT, Palladium.id("lead_ingot_lead_nuggets").toString(), "lead_ingot", leadEnabled
        );

        this.oreProcessing(TITANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, PalladiumItems.TITANIUM_INGOT, 1.5F, 300, "titanium_ingot", titaniumEnabled);
        this.nineBlockStorageRecipes(RecipeCategory.MISC, PalladiumItems.RAW_TITANIUM, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.RAW_TITANIUM_BLOCK, titaniumEnabled);
        this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
                RecipeCategory.MISC, PalladiumItems.TITANIUM_INGOT, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.TITANIUM_BLOCK, Palladium.id("titanium_ingot_from_titanium_block").toString(), "titanium_ingot", titaniumEnabled
        );
        this.nineBlockStorageRecipesWithCustomPacking(
                RecipeCategory.MISC, PalladiumItems.TITANIUM_NUGGET, RecipeCategory.MISC, PalladiumItems.TITANIUM_INGOT, Palladium.id("titanium_ingot_titanium_nuggets").toString(), "titanium_ingot", titaniumEnabled
        );

        this.oreProcessing(VIBRANIUM_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, PalladiumItems.VIBRANIUM_INGOT, 1.5F, 200, "vibranium_ingot", vibraniumEnabled);
        this.nineBlockStorageRecipes(RecipeCategory.MISC, PalladiumItems.RAW_VIBRANIUM, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.RAW_VIBRANIUM_BLOCK, vibraniumEnabled);
        this.nineBlockStorageRecipesRecipesWithCustomUnpacking(
                RecipeCategory.MISC, PalladiumItems.VIBRANIUM_INGOT, RecipeCategory.BUILDING_BLOCKS, PalladiumItems.VIBRANIUM_BLOCK, Palladium.id("vibranium_ingot_from_vibranium_block").toString(), "vibranium_ingot", vibraniumEnabled
        );
        this.nineBlockStorageRecipesWithCustomPacking(
                RecipeCategory.MISC, PalladiumItems.VIBRANIUM_NUGGET, RecipeCategory.MISC, PalladiumItems.VIBRANIUM_INGOT, Palladium.id("vibranium_ingot_vibranium_nuggets").toString(), "vibranium_ingot", vibraniumEnabled
        );

        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_COAL_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.COAL, 0.1F, 200, "coal", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_IRON_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.IRON_INGOT, 0.7F, 200, "iron_ingot", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_COPPER_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.COPPER_INGOT, 0.7F, 200, "copper_ingot", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_GOLD_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.GOLD_INGOT, 1F, 200, "gold_ingot", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_DIAMOND_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.DIAMOND, 1F, 200, "diamond", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_LAPIS_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.LAPIS_LAZULI, 0.2F, 200, "lapis_lazuli", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_REDSTONE_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.REDSTONE, 0.7F, 200, "redstone", this.output);
        this.oreProcessing(Collections.singletonList(PalladiumItems.METEORITE_EMERALD_ORE), RecipeCategory.MISC, CookingBookCategory.MISC, Items.EMERALD, 1F, 200, "emerald", this.output);

        // Fabrics
        for (DyeColor color : DyeColor.values()) {
            this.shaped(RecipeCategory.MISC, PalladiumItems.FABRIC_BY_COLOR.get(color).get(), 8).pattern(" WS").pattern("WXW").pattern("SW ").define('W', getWoolBlockByColor(color)).define('S', Tags.Items.RODS_WOODEN).define('X', Tags.Items.STRINGS).unlockedBy(getHasName(getDyeByColor(color)), has(PalladiumItemTags.DYE_BY_COLOR.get(color))).group("fabrics").save(this.output.withConditions(PalladiumFeatureFlags.conditionTailoring()));
            this.shapeless(RecipeCategory.MISC, PalladiumItems.FABRIC_BY_COLOR.get(color).get()).requires(PalladiumItemTags.FABRICS).requires(PalladiumItemTags.DYE_BY_COLOR.get(color)).unlockedBy(getHasName(getDyeByColor(color)), has(PalladiumItemTags.DYE_BY_COLOR.get(color))).group("dye_fabrics").save(this.output.withConditions(PalladiumFeatureFlags.conditionTailoring()), Palladium.id("fabric_recoloring_" + color.getName()).toString());
            this.shapeless(RecipeCategory.MISC, PalladiumItems.FABRIC_BY_COLOR.get(color).get(), 8).requires(Ingredient.of(this.items.getOrThrow(PalladiumItemTags.FABRICS)), 8).requires(PalladiumItemTags.DYE_BY_COLOR.get(color)).unlockedBy(getHasName(getDyeByColor(color)), has(PalladiumItemTags.DYE_BY_COLOR.get(color))).group("dye_fabrics").save(this.output.withConditions(PalladiumFeatureFlags.conditionTailoring()), Palladium.id("fabric_recoloring_8_" + color.getName()).toString());
        }
    }

    private static Item getWoolBlockByColor(DyeColor color) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName() + "_wool"));
    }

    private static Item getDyeByColor(DyeColor color) {
        return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(color.getName() + "_dye"));
    }

    protected void nineBlockStorageRecipes(RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, RecipeOutput output) {
        this.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, Palladium.MOD_ID + ":" + getSimpleRecipeName(packed), null, Palladium.MOD_ID + ":" + getSimpleRecipeName(unpacked), null, output);
    }

    protected void nineBlockStorageRecipesRecipesWithCustomUnpacking(
            RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String unpackedName, String unpackedGroup, RecipeOutput output
    ) {
        this.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, Palladium.MOD_ID + ":" + getSimpleRecipeName(packed), null, unpackedName, unpackedGroup, output);
    }

    protected void nineBlockStorageRecipesWithCustomPacking(
            RecipeCategory unpackedCategory, ItemLike unpacked, RecipeCategory packedCategory, ItemLike packed, String packedName, String packedGroup, RecipeOutput output
    ) {
        this.nineBlockStorageRecipes(unpackedCategory, unpacked, packedCategory, packed, packedName, packedGroup, Palladium.MOD_ID + ":" + getSimpleRecipeName(unpacked), null, output);
    }

    protected void nineBlockStorageRecipes(RecipeCategory unpackedFormCategory, ItemLike unpackedForm, RecipeCategory packedFormCategory, ItemLike packedForm, String packingRecipeId, @Nullable String packingRecipeGroup, String unpackingRecipeId, @Nullable String unpackingRecipeGroup, RecipeOutput output) {
        this.shapeless(unpackedFormCategory, unpackedForm, 9).requires(packedForm).group(unpackingRecipeGroup).unlockedBy(getHasName(packedForm), this.has(packedForm)).save(output, ResourceKey.create(Registries.RECIPE, Identifier.parse(unpackingRecipeId)));
        this.shaped(packedFormCategory, packedForm).define('#', unpackedForm).pattern("###").pattern("###").pattern("###").group(packingRecipeGroup).unlockedBy(getHasName(unpackedForm), this.has(unpackedForm)).save(output, ResourceKey.create(Registries.RECIPE, Identifier.parse(packingRecipeId)));
    }

    @Override
    protected void stonecutterResultFromBase(RecipeCategory category, ItemLike result, ItemLike material, int resultCount) {
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(material), category, result, resultCount)
                .unlockedBy(getHasName(material), this.has(material))
                .save(this.output, Palladium.MOD_ID + ":" + getConversionRecipeName(result, material) + "_stonecutting");
    }

    protected void oreProcessing(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, RecipeOutput output) {
        this.oreSmelting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, output);
        this.oreBlasting(smeltables, craftingCategory, cookingCategory, result, experience, cookingTime / 2, group, output);
    }

    protected void oreSmelting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, RecipeOutput output) {
        this.oreCooking(SmeltingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_smelting", output);
    }

    protected void oreBlasting(List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, RecipeOutput output) {
        this.oreCooking(BlastingRecipe::new, smeltables, craftingCategory, cookingCategory, result, experience, cookingTime, group, "_from_blasting", output);
    }

    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables, RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result, float experience, int cookingTime, String group, String fromDesc, RecipeOutput output) {
        for (ItemLike item : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(item), craftingCategory, cookingCategory, result, experience, cookingTime, factory)
                    .group(group)
                    .unlockedBy(getHasName(item), this.has(item))
                    .save(output, Palladium.MOD_ID + ":" + getItemName(result) + fromDesc + "_" + getItemName(item));
        }
    }

    public static class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new PalladiumRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Palladium Recipes";
        }
    }

}
