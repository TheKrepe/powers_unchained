package lucraft.mods.lucraftcore.utilities.jei.extractor;

import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.recipes.ExtractorRecipeHandler;
import lucraft.mods.lucraftcore.utilities.recipes.IExtractorRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtractorRecipeWrapper implements IRecipeWrapper {

    public final IExtractorRecipe recipe;

    public ExtractorRecipeWrapper(IExtractorRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack stack : recipe.getInput().getMatchingStacks()) {
            ItemStack s = stack.copy();
            s.setCount(recipe.getInputAmount());
            inputs.add(s);
        }
        ingredients.setInputLists(ItemStack.class, Arrays.asList(Arrays.asList(inputs), Arrays.asList(recipe.getInputContainer().getMatchingStacks())));
        ingredients.setInput(FluidStack.class, recipe.getInputFluid());
        ingredients.setOutputLists(ItemStack.class, Arrays.asList(Arrays.asList(recipe.getPrimaryResult()), Arrays.asList(recipe.getSecondaryResult())));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String s = StringHelper.translateToLocal("lucraftcore.info.energy_display", this.recipe.getRequiredEnergy(), EnergyUtil.ENERGY_UNIT);
        minecraft.fontRenderer.drawString(s, 135 / 2 - minecraft.fontRenderer.getStringWidth(s) / 2, recipeHeight - 11, 4210752);
    }

    public static List<ExtractorRecipeWrapper> getRecipes() {
        List<ExtractorRecipeWrapper> recipes = new ArrayList<>();

        for (IExtractorRecipe recipe : ExtractorRecipeHandler.getRecipes()) {
            recipes.add(new ExtractorRecipeWrapper(recipe));
        }

        return recipes;
    }
}
