package com.star_1mp.powersunleashed.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.CuriosApi;

/** Common-side Curios access. Only class-loaded when Curios is installed. */
public final class CuriosCompat {

    private CuriosCompat() {
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).map(inventory -> inventory.isEquipped(item)).orElse(false);
    }

    public static ItemStack find(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).flatMap(inventory -> inventory.findFirstCurio(item))
                .map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }

    /** Changes the first worn {@code item} and sets it back into its curio slot (which syncs it). */
    public static boolean modify(LivingEntity entity, Item item, java.util.function.Consumer<ItemStack> change) {
        return CuriosApi.getCuriosInventory(entity).flatMap(inventory -> inventory.findFirstCurio(item).map(result -> {
            ItemStack stack = result.stack().copy();
            change.accept(stack);
            inventory.setEquippedCurio(result.slotContext().identifier(), result.slotContext().index(), stack);
            return true;
        })).orElse(false);
    }
}
