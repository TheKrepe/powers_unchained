package net.threetag.palladium.item.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MultiversalIteratorRecipe extends Recipe<MultiversalIteratorRecipeInput> {

    List<Conversion> getConversions(MultiversalIteratorRecipeInput input, HolderLookup.Provider registries);

    @Override
    default boolean matches(MultiversalIteratorRecipeInput input, Level level) {
        return !this.getConversions(input, level.registryAccess()).isEmpty();
    }

    @Override
    default ItemStack assemble(MultiversalIteratorRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    default @NotNull RecipeType<? extends Recipe<MultiversalIteratorRecipeInput>> getType() {
        return PalladiumRecipeTypes.MULTIVERSAL_ITERATOR.get();
    }

    @Override
    @NotNull
    RecipeSerializer<? extends Recipe<MultiversalIteratorRecipeInput>> getSerializer();

    @Override
    default @NotNull RecipeBookCategory recipeBookCategory() {
        return PalladiumRecipeBookCategories.MULTIVERSAL_ITERATOR.get();
    }

    @Override
    default @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    record Conversion(Identifier key, ItemStack result) {
    }
}
