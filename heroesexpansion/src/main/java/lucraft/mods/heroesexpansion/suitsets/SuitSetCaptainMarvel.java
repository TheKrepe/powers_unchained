package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.lucraftcore.utilities.items.UtilitiesItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SuitSetCaptainMarvel extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();

    public SuitSetCaptainMarvel(String name) {
        super(name);
        this.data.setFloat("kree_hybrid_multiplier", 2F);
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public boolean hasArmorOn(EntityLivingBase entity) {
        boolean hasArmorOn = true;

        if (getLegs() != null && (entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty() || entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() != getLegs()))
            hasArmorOn = false;

        if (getBoots() != null && (entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty() || entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() != getBoots()))
            hasArmorOn = false;

        return hasArmorOn;
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(UtilitiesItems.TRI_POLYMER.get(EnumDyeColor.RED));
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return ItemArmor.ArmorMaterial.DIAMOND;
    }

    @Override
    public NBTTagCompound getData() {
        return this.data;
    }

}
