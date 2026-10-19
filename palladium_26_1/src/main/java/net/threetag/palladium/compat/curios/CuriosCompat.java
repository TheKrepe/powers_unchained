package net.threetag.palladium.compat.curios;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public class CuriosCompat {

    public static CuriosCompat INSTANCE = new CuriosCompat();

    public List<String> getSlots(Level level) {
        return Collections.emptyList();
    }

    public List<ItemStack> getFromSlot(LivingEntity entity, String slot) {
        return Collections.emptyList();
    }

    public ItemStack getFromSlot(LivingEntity entity, String slot, int index) {
        return ItemStack.EMPTY;
    }

    public void setInSlot(LivingEntity entity, String slot, int index, ItemStack stack) {

    }

    public int getSlotSize(LivingEntity entity, String slot) {
        return 0;
    }

    public void clearSlot(LivingEntity entity, String slot) {

    }

}
