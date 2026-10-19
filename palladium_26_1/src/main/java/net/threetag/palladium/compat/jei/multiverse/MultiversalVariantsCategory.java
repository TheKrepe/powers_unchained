package net.threetag.palladium.compat.jei.multiverse;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.screen.menu.MultiversalIteratorScreen;
import net.threetag.palladium.item.PalladiumItems;

public class MultiversalVariantsCategory implements IRecipeCategory<MultiversalVariantRecipe> {

    public static final IRecipeType<MultiversalVariantRecipe> RECIPE_TYPE =
            IRecipeType.create(Palladium.MOD_ID, "multiversal_variants", MultiversalVariantRecipe.class);

    private static final int BG_WIDTH = 108;
    private static final int BG_HEIGHT = 54;

    private final IDrawable icon;
    private final IDrawable background;

    public MultiversalVariantsCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(PalladiumItems.MULTIVERSAL_ITERATOR.get().getDefaultInstance());
        this.background = guiHelper.createDrawable(MultiversalIteratorScreen.TEXTURE, 34, 16, BG_WIDTH, BG_HEIGHT);
    }

    @Override
    public IRecipeType<MultiversalVariantRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.palladium.category.multiversal_variants");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return BG_WIDTH;
    }

    @Override
    public int getHeight() {
        return BG_HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiversalVariantRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 4).add(recipe.extrapolator());
        builder.addSlot(RecipeIngredientRole.INPUT, 14, 33).addItemStacks(recipe.inputs());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 74, 33).add(recipe.result());
    }

    @Override
    public void draw(MultiversalVariantRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        guiGraphics.text(Minecraft.getInstance().font, recipe.universeTitle(), 27, 8, 0xffffffff);
    }
}
