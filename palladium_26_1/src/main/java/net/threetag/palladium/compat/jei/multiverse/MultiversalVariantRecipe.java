package net.threetag.palladium.compat.jei.multiverse;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.item.recipe.MultiversalConversionRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipes;
import net.threetag.palladium.item.recipe.MultiversalVariantsRecipe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MultiversalVariantRecipe(ItemStack extrapolator, List<ItemStack> inputs, ItemStack result, Component universeTitle) {

    public static List<MultiversalVariantRecipe> gather(Level level) {
        List<MultiversalVariantRecipe> recipes = new ArrayList<>();

        for (RecipeHolder<MultiversalIteratorRecipe> holder : MultiversalIteratorRecipes.getRecipes(level)) {
            switch (holder.value()) {
                case MultiversalVariantsRecipe ignored -> recipes.addAll(gatherVariantEntries(level.registryAccess()));
                case MultiversalConversionRecipe conversion -> recipes.add(fromConversion(conversion, level));
                default -> {
                }
            }
        }

        return recipes;
    }

    private static MultiversalVariantRecipe fromConversion(MultiversalConversionRecipe recipe, Level level) {
        var inputs = recipe.input().display().resolveForStacks(SlotDisplayContext.fromLevel(level));
        return new MultiversalVariantRecipe(designatedExtrapolator(recipe.universe()), inputs, recipe.result().create(), Universe.getTitle(recipe.universe().identifier()));
    }

    private static List<MultiversalVariantRecipe> gatherVariantEntries(HolderLookup.Provider registries) {
        List<MultiversalVariantRecipe> recipes = new ArrayList<>();
        var lookup = registries.lookup(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT);

        if (lookup.isEmpty()) {
            return recipes;
        }

        Map<Identifier, List<Item>> itemsByCategory = new HashMap<>();

        for (Item item : BuiltInRegistries.ITEM) {
            var category = item.components().get(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY.get());

            if (category != null) {
                itemsByCategory.computeIfAbsent(category, c -> new ArrayList<>()).add(item);
            }
        }

        var variants = lookup.get().listElements()
                .sorted(Comparator.comparing(ref -> ref.key().identifier().toString()))
                .toList();

        for (Holder.Reference<MultiversalItemVariant> variant : variants) {
            var extrapolator = designatedExtrapolator(variant.value().universe());
            var universeTitle = Universe.getTitle(variant.value().universe().identifier());

            for (Identifier category : variant.value().categories().keySet()) {
                for (Item item : itemsByCategory.getOrDefault(category, List.of())) {
                    var result = item.getDefaultInstance();
                    MultiversalItemVariant.apply(registries, result, variant.key());
                    recipes.add(new MultiversalVariantRecipe(extrapolator, List.of(item.getDefaultInstance()), result, universeTitle));
                }
            }
        }

        return recipes;
    }

    private static ItemStack designatedExtrapolator(ResourceKey<Universe> universe) {
        var extrapolator = PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get().getDefaultInstance();
        MultiversalExtrapolatorItem.setUniverse(extrapolator, universe);
        return extrapolator;
    }
}
