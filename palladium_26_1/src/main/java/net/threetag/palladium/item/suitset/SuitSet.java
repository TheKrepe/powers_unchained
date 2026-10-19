package net.threetag.palladium.item.suitset;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.Optional;

public record SuitSet(Optional<Item> helmet, Optional<Item> chestplate, Optional<Item> leggings, Optional<Item> boots) {

    public boolean isWearing(LivingEntity entity) {
        if (this.helmet.isPresent() && !entity.getItemBySlot(EquipmentSlot.HEAD).is(this.helmet.get()))
            return false;
        if (this.chestplate.isPresent() && !entity.getItemBySlot(EquipmentSlot.CHEST).is(this.chestplate.get()))
            return false;
        if (this.leggings.isPresent() && !entity.getItemBySlot(EquipmentSlot.LEGS).is(this.leggings.get()))
            return false;
        return this.boots.isEmpty() || entity.getItemBySlot(EquipmentSlot.FEET).is(this.boots.get());
    }

}
