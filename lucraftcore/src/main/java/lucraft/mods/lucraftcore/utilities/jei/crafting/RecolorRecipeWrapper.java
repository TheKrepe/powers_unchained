package lucraft.mods.lucraftcore.utilities.jei.crafting;

import lucraft.mods.lucraftcore.util.recipe.RecipeFactoryRecolor;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.recipes.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecolorRecipeWrapper implements ICraftingRecipeWrapper {

    private final IJeiHelpers jeiHelpers;
    protected final RecipeFactoryRecolor.RecipeRecolor recipe;

    public RecolorRecipeWrapper(IJeiHelpers jeiHelpers, RecipeFactoryRecolor.RecipeRecolor recipe) {
        this.jeiHelpers = jeiHelpers;
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack recipeOutput = recipe.result;
        IStackHelper stackHelper = jeiHelpers.getStackHelper();
        List<Ingredient> list = new ArrayList<>();
        list.add(Ingredient.fromItem(this.recipe.original));
        list.addAll(this.recipe.ingredientList);

        try {
            List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(list);
            ingredients.setInputLists(ItemStack.class, inputLists);
            ingredients.setOutput(ItemStack.class, recipeOutput);
        } catch (RuntimeException e) {
            String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, list, recipeOutput);
            throw new BrokenCraftingRecipeException(info, e);
        }
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return recipe.getRegistryName();
    }
}