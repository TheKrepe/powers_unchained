package lucraft.mods.lucraftcore.utilities.recipes;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class BoilerRecipe implements IBoilerRecipe {

    protected FluidStack result;
    protected FluidStack input;
    protected Ingredient[] ingredients;
    protected int energy;
    protected ResourceLocation registryName;

    public BoilerRecipe(FluidStack result, int energy, FluidStack input, Ingredient... ingredients) {
        this.result = result;
        this.input = input;
        this.ingredients = ingredients;
        this.energy = energy;
    }

    @Override
    public FluidStack getResult() {
        return this.result;
    }

    @Override
    public Ingredient[] getIngredients() {
        return this.ingredients;
    }

    @Override
    public FluidStack getInputFluid() {
        return this.input;
    }

    @Override
    public int getRequiredEnergy() {
        return this.energy;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    public BoilerRecipe setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
        return this;
    }
}
