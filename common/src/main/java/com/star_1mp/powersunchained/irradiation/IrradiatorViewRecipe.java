package com.star_1mp.powersunchained.irradiation;

import com.star_1mp.powersunchained.registry.PUItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** What JEI / REI show for the Irradiator: the cave spider mutation and the item recipes, with their chances. */
public record IrradiatorViewRecipe(ItemStack input, ItemStack output, int chancePercent, boolean creature) {

    public static List<IrradiatorViewRecipe> all() {
        List<IrradiatorViewRecipe> list = new ArrayList<>();
        list.add(new IrradiatorViewRecipe(new ItemStack(Items.CAVE_SPIDER_SPAWN_EGG), new ItemStack(PUItems.RADIOACTIVE_SPIDER_SPAWN_EGG.get()),
                Irradiation.chancePercent(Irradiation.KIND_CAVE_SPIDER), true));
        for (IrradiationRecipes.Recipe r : IrradiationRecipes.all()) {
            list.add(new IrradiatorViewRecipe(new ItemStack(r.input().get()), new ItemStack(r.result().get()), Math.round(r.chance() * 100.0F), false));
        }
        return list;
    }
}
