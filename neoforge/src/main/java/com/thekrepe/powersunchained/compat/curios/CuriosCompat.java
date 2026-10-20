package com.thekrepe.powersunchained.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

/** Common-side Curios access. Only class-loaded when Curios is installed. */
public final class CuriosCompat {

    private CuriosCompat() {
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(inventory -> inventory.isEquipped(item)).orElse(false);
    }
}
