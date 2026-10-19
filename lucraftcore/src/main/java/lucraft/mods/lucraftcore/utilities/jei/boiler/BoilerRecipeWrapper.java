package lucraft.mods.lucraftcore.utilities.jei.boiler;

import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.recipes.BoilerRecipeHandler;
import lucraft.mods.lucraftcore.utilities.recipes.IBoilerRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class BoilerRecipeWrapper implements IRecipeWrapper {

    private final IJeiHelpers jeiHelpers;
    public IBoilerRecipe recipe;

    public BoilerRecipeWrapper(IJeiHelpers jeiHelpers, IBoilerRecipe recipe) {
        this.jeiHelpers = jeiHelpers;
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        FluidStack recipeInput = recipe.getInputFluid();
        FluidStack recipeOutput = recipe.getResult();
        IStackHelper stackHelper = jeiHelpers.getStackHelper();

        NonNullList<Ingredient> inputs = NonNullList.create();
        for (Ingredient ingredient : recipe.getIngredients())
            inputs.add(ingredient);
        List<List<ItemStack>> inputLists = stackHelper.expandRecipeItemStackInputs(inputs);
        ingredients.setInput(FluidStack.class, recipeInput);
        ingredients.setInputLists(ItemStack.class, inputLists);
        ingredients.setOutput(FluidStack.class, recipeOutput);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String s = StringHelper.translateToLocal("lucraftcore.info.energy_display", this.recipe.getRequiredEnergy(), EnergyUtil.ENERGY_UNIT);
        minecraft.fontRenderer.drawString(s, 134 / 2 - minecraft.fontRenderer.getStringWidth(s) / 2, recipeHeight - 6, 4210752);
    }

    public static List<BoilerRecipeWrapper> getRecipes(IJeiHelpers jeiHelpers) {
        List<BoilerRecipeWrapper> recipes = new ArrayList<>();

        for (IBoilerRecipe recipe : BoilerRecipeHandler.getRecipes()) {
            recipes.add(new BoilerRecipeWrapper(jeiHelpers, recipe));
        }

        return recipes;
    }

}
