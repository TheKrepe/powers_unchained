package lucraft.mods.lucraftcore.utilities.jei.boiler;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityBoiler;
import lucraft.mods.lucraftcore.utilities.jei.LCJEIPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class BoilerRecipeCategory implements IRecipeCategory<BoilerRecipeWrapper> {

    private final IDrawable background;
    private final IDrawableStatic tankOverlay;
    private final String title;

    public BoilerRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(LCJEIPlugin.TEXTURE, 0, 98, 134, 62);
        this.tankOverlay = guiHelper.createDrawable(LCJEIPlugin.TEXTURE, 176, 1, 16, 60);
        this.title = "tile.boiler.name";
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public String getTitle() {
        return StringHelper.translateToLocal(this.title);
    }

    @Override
    public String getUid() {
        return LCJEIPlugin.BOILER;
    }

    @Override
    public String getModName() {
        return LucraftCore.NAME;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, BoilerRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

        fluidStacks.init(0, true, 1, 1, 16, 60, TileEntityBoiler.TANK_CAPACITY, true, this.tankOverlay);
        fluidStacks.init(1, false, 117, 1, 16, 60, TileEntityBoiler.TANK_CAPACITY, true, this.tankOverlay);

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                int index = x + (y * 3);
                itemStacks.init(index, true, 24 + x * 18, 0 + y * 18);
            }
        }

        itemStacks.set(ingredients);
        fluidStacks.set(0, ingredients.getInputs(FluidStack.class).get(0));
        fluidStacks.set(1, ingredients.getOutputs(FluidStack.class).get(0));
    }
}
