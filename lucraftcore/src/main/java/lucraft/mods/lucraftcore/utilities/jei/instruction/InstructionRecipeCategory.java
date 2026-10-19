package lucraft.mods.lucraftcore.utilities.jei.instruction;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.utilities.blocks.UtilitiesBlocks;
import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import lucraft.mods.lucraftcore.utilities.jei.LCJEIPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class InstructionRecipeCategory implements IRecipeCategory<InstructionRecipeWrapper> {

    private static final int input = 0;
    private static final int output = 1;
    private final IDrawable background;
    private final IDrawable icon;
    private final String title;

    public InstructionRecipeCategory(IGuiHelper guiHelper) {
        title = "tile.extractor.name";
        background = guiHelper.drawableBuilder(LCJEIPlugin.TEXTURE, 0, 62, 70, 18)
                .addPadding(0, 20, 0, 0)
                .build();
        icon = guiHelper.createDrawableIngredient(new ItemStack(UtilitiesBlocks.CONSTRUCTION_TABLE));
    }

    @Override
    public String getUid() {
        return LCJEIPlugin.INSTRUCTION;
    }

    @Override
    public String getTitle() {
        return UtilitiesItems.INSTRUCTION.getItemStackDisplayName(new ItemStack(UtilitiesItems.INSTRUCTION));
    }

    @Override
    public String getModName() {
        return LucraftCore.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        this.icon.draw(minecraft, 1, 20);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, InstructionRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(input, true, 0, 0);
        guiItemStacks.init(output, false, 52, 0);

        guiItemStacks.set(ingredients);
    }

}
