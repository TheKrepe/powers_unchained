package net.threetag.palladium.client.recipe;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.recipe.ExtrapolatorCraftingRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipes;
import net.threetag.palladium.item.recipe.PalladiumRecipeTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ClientMultiversalIteratorRecipes {

    private static List<RecipeHolder<ExtrapolatorCraftingRecipe>> EXTRAPOLATOR_CRAFTING_RECIPES = List.of();

    @SubscribeEvent
    static void recipesReceived(RecipesReceivedEvent e) {
        List<RecipeHolder<MultiversalIteratorRecipe>> recipes = new ArrayList<>(e.getRecipeMap().byType(PalladiumRecipeTypes.MULTIVERSAL_ITERATOR.get()));
        MultiversalIteratorRecipes.setSyncedRecipes(recipes);

        EXTRAPOLATOR_CRAFTING_RECIPES = e.getRecipeMap().byType(RecipeType.CRAFTING).stream()
                .filter(holder -> holder.value() instanceof ExtrapolatorCraftingRecipe)
                .map(holder -> {
                    @SuppressWarnings("unchecked")
                    var cast = (RecipeHolder<ExtrapolatorCraftingRecipe>) (RecipeHolder<?>) holder;
                    return cast;
                })
                .sorted(Comparator.comparing(holder -> holder.id().identifier().toString()))
                .toList();
    }

    @SubscribeEvent
    static void clientLogOut(ClientPlayerNetworkEvent.LoggingOut e) {
        MultiversalIteratorRecipes.setSyncedRecipes(List.of());
        EXTRAPOLATOR_CRAFTING_RECIPES = List.of();
    }

    public static List<RecipeHolder<ExtrapolatorCraftingRecipe>> getExtrapolatorCraftingRecipes() {
        return EXTRAPOLATOR_CRAFTING_RECIPES;
    }
}
