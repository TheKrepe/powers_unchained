package com.star_1mp.powersunleashed.compat.jei;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.client.suitcreator.SuitRecipeArt;
import com.star_1mp.powersunleashed.client.techtable.TechRecipeArt;
import com.star_1mp.powersunleashed.tech.TechRecipe;
import com.star_1mp.powersunleashed.tech.TechRecipes;
import com.star_1mp.powersunleashed.registry.PUItems;
import com.star_1mp.powersunleashed.suit.SuitRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** JEI: the Suit Creator's blueprints as a recipe category (only loaded by JEI, which is optional). */
@JeiPlugin
public class PUJeiPlugin implements IModPlugin {

    public static final IRecipeType<SuitRecipe> SUIT_CREATOR = IRecipeType.create(Constants.MOD_ID, "suit_creator", SuitRecipe.class);
    public static final IRecipeType<TechRecipe> TECH_TABLE = IRecipeType.create(Constants.MOD_ID, "tech_table", TechRecipe.class);
    public static final IRecipeType<com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe> IRRADIATOR = IRecipeType.create(
            Constants.MOD_ID, "irradiator", com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe.class);

    @Override
    public Identifier getPluginUid() {
        return Constants.id("jei_plugin");
    }

    private static mezz.jei.api.runtime.IJeiRuntime runtime;

    static {
        // loaded by JEI before it reads its config: Suit Creator right after crafting in JEI's category order
        JeiCategoryOrder.suitCreatorFirst(SUIT_CREATOR.getUid().toString());
    }

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    /** Dev/CI only: opens the Suit Creator category, or the recipes of an item if given. */
    public static void debugShow(net.minecraft.world.item.@org.jspecify.annotations.Nullable ItemStack output) {
        if (runtime == null) {
            Constants.LOG.warn("[CI] JEI runtime not available");
            return;
        }
        if (output == null) {
            runtime.getRecipesGui().showTypes(java.util.List.of(SUIT_CREATOR));
        } else {
            // like pressing R on the item: JEI picks the category order itself
            runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(
                    mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT, mezz.jei.api.constants.VanillaTypes.ITEM_STACK, output));
        }
    }

    private static SuitCreatorCategory category;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        category = new SuitCreatorCategory(registration.getJeiHelpers().getGuiHelper());
        registration.addRecipeCategories(category, new TechTableCategory(registration.getJeiHelpers().getGuiHelper()),
                new IrradiatorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SUIT_CREATOR, SuitRecipe.all());
        registration.addRecipes(TECH_TABLE, TechRecipes.viewerRecipes());
        registration.addRecipes(IRRADIATOR, com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe.all());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(SUIT_CREATOR, PUItems.SUIT_CREATOR.get());
        registration.addCraftingStation(TECH_TABLE, PUItems.TECH_TABLE.get());
        registration.addCraftingStation(IRRADIATOR, PUItems.IRRADIATOR.get());
    }

    /** Dev/CI only: opens the Tech Table category. */
    public static void debugShowTechTable() {
        if (runtime != null) {
            runtime.getRecipesGui().showTypes(java.util.List.of(TECH_TABLE));
        }
    }

    /** Tech Table recipes on blueprint paper ({@link TechRecipeArt}). */
    static final class TechTableCategory extends AbstractRecipeCategory<TechRecipe> {

        TechTableCategory(IGuiHelper gui) {
            super(TECH_TABLE, Component.translatable("block.powers_unleashed.tech_table"),
                    gui.createDrawableItemLike(PUItems.TECH_TABLE.get()), TechRecipeArt.WIDTH, TechRecipeArt.HEIGHT);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, TechRecipe recipe, IFocusGroup focuses) {
            var layout = recipe.layout();
            for (int i = 0; i < 9; i++) {
                if (layout.get(i) != null) {
                    int[] s = TechRecipeArt.cellSlot(i);
                    builder.addInputSlot(s[0], s[1]).addItemStacks(layout.get(i).display().get());
                }
            }
            builder.addOutputSlot(TechRecipeArt.RESULT[0], TechRecipeArt.RESULT[1]).add(recipe.resultStack());
        }

        @Override
        public void draw(TechRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
            TechRecipeArt.draw(graphics, recipe, 0, 0);
        }
    }

    /** The Irradiator: sample -> result with the chance ({@link com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt}). */
    static final class IrradiatorCategory extends AbstractRecipeCategory<com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe> {

        IrradiatorCategory(IGuiHelper gui) {
            super(IRRADIATOR, Component.translatable("block.powers_unleashed.irradiator"), gui.createDrawableItemLike(PUItems.IRRADIATOR.get()),
                    com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt.WIDTH,
                    com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt.HEIGHT);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe recipe, IFocusGroup focuses) {
            int[] in = com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt.INPUT;
            int[] out = com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt.OUTPUT;
            builder.addInputSlot(in[0], in[1]).add(recipe.input());
            builder.addOutputSlot(out[0], out[1]).add(recipe.output());
        }

        @Override
        public void draw(com.star_1mp.powersunleashed.irradiation.IrradiatorViewRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics,
                double mouseX, double mouseY) {
            com.star_1mp.powersunleashed.client.irradiation.IrradiatorRecipeArt.draw(graphics, recipe, 0, 0);
        }
    }

    static final class SuitCreatorCategory extends AbstractRecipeCategory<SuitRecipe> {

        SuitCreatorCategory(IGuiHelper gui) {
            super(SUIT_CREATOR, Component.translatable("block.powers_unleashed.suit_creator"),
                    gui.createDrawableItemLike(PUItems.SUIT_CREATOR.get()), SuitRecipeArt.WIDTH, SuitRecipeArt.HEIGHT);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, SuitRecipe recipe, IFocusGroup focuses) {
            for (int i = 0; i < Math.min(8, recipe.inputs().size()); i++) {
                int[] s = SuitRecipeArt.inputSlot(i);
                builder.addInputSlot(s[0], s[1]).add(recipe.inputs().get(i));
            }
            for (int i = 0; i < recipe.outputs().size(); i++) {
                int[] s = SuitRecipeArt.outputSlot(recipe, i);
                builder.addOutputSlot(s[0], s[1]).add(recipe.outputs().get(i));
            }
        }

        @Override
        public void draw(SuitRecipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
            SuitRecipeArt.draw(graphics, recipe, 0, 0);
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, SuitRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
            if (SuitRecipeArt.inUnlockBox(mouseX, mouseY)) {
                tooltip.addAll(SuitRecipeArt.unlockTooltip(recipe));
            }
        }
    }
}
