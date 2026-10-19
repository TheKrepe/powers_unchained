package lucraft.mods.lucraftcore.utilities.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public interface IExtractorRecipe {

    Ingredient getInput();

    int getInputAmount();

    Ingredient getInputContainer();

    FluidStack getInputFluid();

    ItemStack getPrimaryResult();

    float getPrimaryChance();

    ItemStack getSecondaryResult();

    float getSecondaryChance();

    int getRequiredEnergy();

    ResourceLocation getRegistryName();

}
