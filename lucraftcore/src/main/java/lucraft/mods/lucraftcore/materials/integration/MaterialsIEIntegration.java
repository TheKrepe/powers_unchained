package lucraft.mods.lucraftcore.materials.integration;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import lucraft.mods.lucraftcore.materials.Material;
import net.minecraft.item.ItemStack;

public class MaterialsIEIntegration {

    public static void postInit() {

        addAlloyRecipe(Material.ADAMANTIUM.getItemStack(Material.MaterialComponent.INGOT, 3), "Steel:2", "Vibranium:1", 400);
        addAlloyRecipe(Material.INTERTIUM.getItemStack(Material.MaterialComponent.INGOT, 3), "Iron:2", "Osmium:1", 200);
        addAlloyRecipe(Material.GOLD_TITANIUM_ALLOY.getItemStack(Material.MaterialComponent.INGOT, 3), "Gold:1", "Titanium:2", 200);

    }

    public static void addAlloyRecipe(ItemStack output, String input1, String input2, int time) {
        String input1Item = input1.split(":")[0];
        int input1Amount = Integer.parseInt(input1.split(":")[1]);
        String input2Item = input2.split(":")[0];
        int input2Amount = Integer.parseInt(input2.split(":")[1]);
        AlloyRecipe.addRecipe(output, new IngredientStack("ingot" + input1Item, input1Amount), new IngredientStack("ingot" + input2Item, input2Amount), time);
        AlloyRecipe.addRecipe(output, new IngredientStack("dust" + input1Item, input1Amount), new IngredientStack("dust" + input2Item, input2Amount), time);
        AlloyRecipe.addRecipe(output, new IngredientStack("ingot" + input1Item, input1Amount), new IngredientStack("dust" + input2Item, input2Amount), time);
        AlloyRecipe.addRecipe(output, new IngredientStack("dust" + input1Item, input1Amount), new IngredientStack("ingot" + input2Item, input2Amount), time);
    }

}