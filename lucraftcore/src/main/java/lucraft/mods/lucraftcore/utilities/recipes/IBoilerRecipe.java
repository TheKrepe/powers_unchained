package lucraft.mods.lucraftcore.utilities.recipes;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public interface IBoilerRecipe {

    FluidStack getResult();

    Ingredient[] getIngredients();

    FluidStack getInputFluid();

    int getRequiredEnergy();

    ResourceLocation getRegistryName();

}
