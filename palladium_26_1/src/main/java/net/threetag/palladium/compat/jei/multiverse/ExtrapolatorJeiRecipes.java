package net.threetag.palladium.compat.jei.multiverse;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.recipe.ClientMultiversalIteratorRecipes;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtrapolatorJeiRecipes {

    public static List<RecipeHolder<CraftingRecipe>> gather(Level level) {
        List<RecipeHolder<CraftingRecipe>> entries = new ArrayList<>();
        var universes = level.registryAccess().lookup(PalladiumRegistryKeys.MULTIVERSE)
                .map(lookup -> lookup.listElements()
                        .sorted(Comparator.comparing(ref -> ref.key().identifier().toString()))
                        .toList())
                .orElseGet(List::of);

        for (var recipeHolder : ClientMultiversalIteratorRecipes.getExtrapolatorCraftingRecipes()) {
            var recipe = recipeHolder.value();

            for (Holder.Reference<Universe> universe : universes) {
                var designated = PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get().getDefaultInstance();
                MultiversalExtrapolatorItem.setUniverse(designated, universe.key());

                List<Ingredient> inputs = new ArrayList<>();
                inputs.add(DataComponentIngredient.of(false, designated));

                for (int i = 0; i < recipe.blankExtrapolatorCount(); i++) {
                    inputs.add(Ingredient.of(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get()));
                }

                for (SizedIngredient sized : recipe.getIngredients()) {
                    for (int i = 0; i < sized.count(); i++) {
                        inputs.add(sized.ingredient());
                    }
                }

                var result = new ItemStackTemplate(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get(),
                        DataComponentPatch.builder().set(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE.get(), universe.key()).build());

                var shapeless = new ShapelessRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), result, inputs);
                entries.add(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, syntheticId(recipeHolder.id().identifier(), universe.key().identifier())), shapeless));
            }
        }

        return entries;
    }

    private static Identifier syntheticId(Identifier recipeId, Identifier universeId) {
        return Palladium.id("jei/" + recipeId.getNamespace() + "/" + recipeId.getPath() + "/" + universeId.getNamespace() + "/" + universeId.getPath());
    }
}
