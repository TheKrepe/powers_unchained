package lucraft.mods.heroesexpansion.integration.jei;

import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;

public class CapShieldRecipeWrapper implements ICraftingRecipeWrapper {

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(ItemStack.class, Arrays.asList(Arrays.asList(new ItemStack(HEItems.VIBRANIUM_SHIELD)), ItemHelper.getOres("dyeRed"), ItemHelper.getOres("dyeBlue"), ItemHelper.getOres("dyeWhite")));
        ingredients.setOutput(ItemStack.class, new ItemStack(HEItems.CAPTAIN_AMERICA_SHIELD));
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation("HeroesExpansion.MODID", "captain_america_shield");
    }
}
