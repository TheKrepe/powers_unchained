package lucraft.mods.lucraftcore.utilities.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class ExtractorRecipe implements IExtractorRecipe {

    protected Ingredient input;
    protected int inputAmount;
    protected Ingredient inputContainer;
    protected FluidStack inputFluid;
    protected ItemStack primaryResult;
    protected float primaryChance;
    protected ItemStack secondaryResult;
    protected float secondaryChance;
    protected int energy;
    protected ResourceLocation registryName;

    public ExtractorRecipe(Ingredient input, int inputAmount, Ingredient inputContainer, FluidStack inputFluid, ItemStack primaryResult, float primaryChance, ItemStack secondaryResult, float secondaryChance, int energy) {
        this.input = input;
        this.inputAmount = inputAmount;
        this.inputContainer = inputContainer;
        this.inputFluid = inputFluid;
        this.primaryResult = primaryResult;
        this.primaryChance = primaryChance;
        this.secondaryResult = secondaryResult;
        this.secondaryChance = secondaryChance;
        this.energy = energy;
    }

    @Override
    public Ingredient getInput() {
        return this.input;
    }

    @Override
    public int getInputAmount() {
        return this.inputAmount;
    }

    @Override
    public Ingredient getInputContainer() {
        return this.inputContainer;
    }

    @Override
    public FluidStack getInputFluid() {
        return this.inputFluid;
    }

    @Override
    public ItemStack getPrimaryResult() {
        return this.primaryResult;
    }

    @Override
    public float getPrimaryChance() {
        return this.primaryChance;
    }

    @Override
    public ItemStack getSecondaryResult() {
        return this.secondaryResult;
    }

    @Override
    public float getSecondaryChance() {
        return this.secondaryChance;
    }

    @Override
    public int getRequiredEnergy() {
        return this.energy;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    public ExtractorRecipe setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
        return this;
    }

}
