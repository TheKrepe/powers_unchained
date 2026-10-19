package net.threetag.palladium.item.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public class MultiversalIteratorRecipes {

    private static List<RecipeHolder<MultiversalIteratorRecipe>> syncedRecipes = List.of();

    public static void setSyncedRecipes(List<RecipeHolder<MultiversalIteratorRecipe>> recipes) {
        syncedRecipes = recipes.stream()
                .sorted(Comparator.comparing(holder -> holder.id().identifier().toString()))
                .toList();
    }

    @SuppressWarnings("unchecked")
    public static List<RecipeHolder<MultiversalIteratorRecipe>> getRecipes(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().getRecipes().stream()
                    .filter(holder -> holder.value().getType() == PalladiumRecipeTypes.MULTIVERSAL_ITERATOR.get())
                    .map(holder -> (RecipeHolder<MultiversalIteratorRecipe>) holder)
                    .sorted(Comparator.comparing(holder -> holder.id().identifier().toString()))
                    .toList();
        }

        return syncedRecipes;
    }
}
