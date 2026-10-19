package lucraft.mods.lucraftcore.utilities.jei.extractor;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityExtractor;
import lucraft.mods.lucraftcore.utilities.jei.LCJEIPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class ExtractorRecipeCategory implements IRecipeCategory<ExtractorRecipeWrapper> {

    private static final int input1 = 0;
    private static final int input2 = 1;
    private static final int output1 = 2;
    private static final int output2 = 3;
    private final String title;
    private final IDrawable background;
    private final IDrawableAnimated arrow;
    private final IDrawableStatic tankOverlay;

    public ExtractorRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.createDrawable(LCJEIPlugin.TEXTURE, 0, 0, 135, 62);
        IDrawableStatic arrowDrawable = guiHelper.createDrawable(LCJEIPlugin.TEXTURE, 135, 0, 40, 23);
        this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, IDrawableAnimated.StartDirection.LEFT, false);
        this.tankOverlay = guiHelper.createDrawable(LCJEIPlugin.TEXTURE, 176, 1, 16, 60);
        title = "tile.extractor.name";
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ExtractorRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();

        itemStacks.init(input1, true, 24, 21);
        itemStacks.init(input2, true, 54, 21);
        fluidStacks.init(0, true, 1, 1, 16, 60, TileEntityExtractor.TANK_CAPACITY, true, this.tankOverlay);
        itemStacks.init(output1, false, 88, 21);
        itemStacks.init(output2, false, 113, 21);

        itemStacks.set(ingredients);
        fluidStacks.set(0, ingredients.getInputs(FluidStack.class).get(0));

        itemStacks.addTooltipCallback((slotIndex, input, itemStack, list) -> {
            if (slotIndex == output1)
                list.add((recipeWrapper.recipe.getPrimaryChance() * 100) + "%");
            if (slotIndex == output2 && !recipeWrapper.recipe.getSecondaryResult().isEmpty())
                list.add((recipeWrapper.recipe.getSecondaryChance() * 100) + "%");
        });
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        arrow.draw(minecraft, 43, 19);
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
        return LCJEIPlugin.EXTRACTOR;
    }

    @Override
    public String getModName() {
        return LucraftCore.NAME;
    }
}
