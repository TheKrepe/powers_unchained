package net.threetag.palladium.compat.jei.tailoring;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.recipe.TailoringRecipe;

public class TailoringCategory implements IRecipeCategory<TailoringRecipe> {

    public static final IRecipeType<TailoringRecipe> RECIPE_TYPE =
            IRecipeType.create(Palladium.MOD_ID, "tailoring", TailoringRecipe.class);

    private static final int BG_WIDTH = 141;
    private static final int BG_HEIGHT = 75;

    private final IDrawable icon;
    private final IGuiHelper guiHelper;

    public TailoringCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(PalladiumItems.TAILORING_BENCH.get().getDefaultInstance());
        this.guiHelper = guiHelper;
    }

    @Override
    public IRecipeType<TailoringRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.palladium.category.tailoring");
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
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, TailoringRecipe recipe, IFocusGroup focuses) {
        // Tool
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 58).setStandardSlotBackground().add(recipe.toolIngredient());

        // Ingredients
        var ingredients = recipe.getIngredients();
        for (SizedIngredient sizedIngredient : ingredients) {
            builder.addInputSlot().setStandardSlotBackground().add(sizedIngredient.ingredient());
        }

        // Results
        var results = recipe.getResults();
        ArmorType[] orderedTypes = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
        for (int i = 0; i < orderedTypes.length; i++) {
            var template = results.get(orderedTypes[i]);
            if (template == null) continue;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 124, 2 + (i * 18)).setStandardSlotBackground().add(template.create());
        }
    }

    @Override
    public void draw(TailoringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        IDrawableStatic recipeArrow = this.guiHelper.getRecipeArrow();
        recipeArrow.draw(guiGraphics, 94, (BG_HEIGHT - recipeArrow.getHeight()) / 2);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, TailoringRecipe recipe, IFocusGroup focuses) {
        var slotsViews = builder.getRecipeSlots();
        var inputSlots = slotsViews.getSlots(RecipeIngredientRole.INPUT);
        inputSlots.removeFirst();
        builder.addScrollGridWidget(inputSlots, 4, 3);
    }
}
