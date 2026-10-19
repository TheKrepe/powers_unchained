package net.threetag.palladium.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.screen.menu.ClientTailoringRecipeBook;
import net.threetag.palladium.client.gui.screen.menu.MultiversalIteratorScreen;
import net.threetag.palladium.client.gui.screen.menu.TailoringScreen;
import net.threetag.palladium.compat.jei.multiverse.ExtrapolatorJeiRecipes;
import net.threetag.palladium.compat.jei.multiverse.MultiversalVariantRecipe;
import net.threetag.palladium.compat.jei.multiverse.MultiversalVariantsCategory;
import net.threetag.palladium.compat.jei.tailoring.TailoringCategory;
import net.threetag.palladium.compat.jei.tailoring.TailoringTransferHandler;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.PalladiumItems;

@JeiPlugin
public class PalladiumJEIPlugin implements IModPlugin {

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.components().has(PalladiumDataComponents.Items.MULTIVERSAL_CATEGORY)) {
                registration.registerFromDataComponentTypes(item, PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get());
            }
        }

        registration.registerFromDataComponentTypes(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get(), PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var jeiHelpers = registration.getJeiHelpers();
        registration.addRecipeCategories(new TailoringCategory(jeiHelpers.getGuiHelper()));
        registration.addRecipeCategories(new MultiversalVariantsCategory(jeiHelpers.getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(TailoringCategory.RECIPE_TYPE, ClientTailoringRecipeBook.getAllKnownRecipes());
        var level = Minecraft.getInstance().level;

        if (level != null) {
            registration.addRecipes(MultiversalVariantsCategory.RECIPE_TYPE, MultiversalVariantRecipe.gather(level));
            registration.addRecipes(RecipeTypes.CRAFTING, ExtrapolatorJeiRecipes.gather(level));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(TailoringCategory.RECIPE_TYPE, PalladiumItems.TAILORING_BENCH);
        registration.addCraftingStation(MultiversalVariantsCategory.RECIPE_TYPE, PalladiumItems.MULTIVERSAL_ITERATOR);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(TailoringScreen.class, 175, 47, 22, 15, TailoringCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(MultiversalIteratorScreen.class, 74, 49, 20, 16, MultiversalVariantsCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new TailoringTransferHandler(registration.getTransferHelper()), TailoringCategory.RECIPE_TYPE);
    }

    @Override
    public Identifier getPluginUid() {
        return Palladium.id("jei");
    }
}
