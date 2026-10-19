package lucraft.mods.lucraftcore.utilities.jei.instruction;

import lucraft.mods.lucraftcore.utilities.items.ItemInstruction;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InstructionRecipeWrapper implements IRecipeWrapper {

    public InstructionRecipe recipe;

    public InstructionRecipeWrapper(InstructionRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack stack = new ItemStack(UtilitiesItems.INSTRUCTION);
        ItemInstruction.setInstructionRecipes(stack, this.recipe);
        ingredients.setInput(ItemStack.class, stack);
        ingredients.setOutput(ItemStack.class, recipe.getOutput());
    }

    public static List<InstructionRecipeWrapper> getRecipes() {
        List<InstructionRecipeWrapper> recipes = new ArrayList<>();

        for (InstructionRecipe recipe : InstructionRecipe.getInstructionRecipes()) {
            recipes.add(new InstructionRecipeWrapper(recipe));
        }

        return recipes;
    }

}
