package lucraft.mods.heroesexpansion.integration.jei;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.utilities.jei.LCAnvilRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

import java.util.Arrays;
import java.util.function.Predicate;

@JEIPlugin
public class HEJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry reg) {
        Predicate<LCAnvilRecipe> predicate = (recipe) -> {
            if ((Loader.isModLoaded("tconstruct") || Loader.isModLoaded("thermalexpansion") || Loader.isModLoaded("immersiveengineering")) && HEConfig.DISABLE_ANVIL_WITH_MODS)
                return false;
            if (!HEConfig.ANVIL_CRAFTING)
                return false;
            return true;
        };
        reg.addRecipes(Arrays.asList(new LCAnvilRecipe(new ItemStack(HEItems.MJOLNIR_HEAD_CAST), ItemHelper.getOres("ingotUru", 6), Arrays.asList(new ItemStack(HEItems.MJOLNIR_HEAD))).setPredicate(predicate)), VanillaRecipeCategoryUid.ANVIL);
        reg.addRecipes(Arrays.asList(new LCAnvilRecipe(new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD_CAST), ItemHelper.getOres("ingotUru", 9), Arrays.asList(new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD))).setPredicate(predicate)), VanillaRecipeCategoryUid.ANVIL);
        reg.addRecipes(Arrays.asList(new LCAnvilRecipe(new ItemStack(HEItems.STORMBREAKER_HEAD_CAST), ItemHelper.getOres("ingotUru", 9), Arrays.asList(new ItemStack(HEItems.STORMBREAKER_HEAD))).setPredicate(predicate)), VanillaRecipeCategoryUid.ANVIL);

        reg.addRecipes(Arrays.asList(new CapShieldRecipeWrapper()), VanillaRecipeCategoryUid.CRAFTING);
    }

}
