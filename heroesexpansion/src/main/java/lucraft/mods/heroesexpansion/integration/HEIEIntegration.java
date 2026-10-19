package lucraft.mods.heroesexpansion.integration;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.items.HEItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class HEIEIntegration {

    public static void init() {
        if (HEConfig.MOD_CRAFTING) {
            for (ItemStack ingots : OreDictionary.getOres("ingotUru")) {
                ItemStack input = ingots.copy();
                input.setCount(6);
                MetalPressRecipe.addRecipe(new ItemStack(HEItems.MJOLNIR_HEAD), input.copy(), new ItemStack(HEItems.MJOLNIR_HEAD_CAST), 2000);
                input.setCount(9);
                MetalPressRecipe.addRecipe(new ItemStack(HEItems.STORMBREAKER_HEAD), input.copy(), new ItemStack(HEItems.STORMBREAKER_HEAD_CAST), 2000);
                MetalPressRecipe.addRecipe(new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD), input.copy(), new ItemStack(HEItems.ULTIMATE_MJOLNIR_HEAD_CAST), 2000);
            }
        }
    }

}
