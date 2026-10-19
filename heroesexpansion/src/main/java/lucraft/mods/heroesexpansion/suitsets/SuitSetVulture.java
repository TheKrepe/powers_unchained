package lucraft.mods.heroesexpansion.suitsets;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;

public class SuitSetVulture extends HESuitSet {

    public SuitSetVulture(String name) {
        super(name);
    }

    @Override
    public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }
}
