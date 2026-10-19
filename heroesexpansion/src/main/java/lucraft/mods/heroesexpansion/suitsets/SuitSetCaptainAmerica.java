package lucraft.mods.heroesexpansion.suitsets;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SuitSetCaptainAmerica extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();

    public SuitSetCaptainAmerica(String name) {
        super(name);
        data.setString("broken_texture", "nomad");
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(Items.IRON_INGOT);
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return ItemArmor.ArmorMaterial.DIAMOND;
    }

    @Override
    public NBTTagCompound getData() {
        return this.data;
    }

    @Override
    public String getArmorTexturePath(ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        String tex = slot == EntityEquipmentSlot.HEAD ? "helmet" : slot == EntityEquipmentSlot.CHEST ? "chestplate" : slot == EntityEquipmentSlot.LEGS ? "legs" : "boots";
        String name = ((float) stack.getItemDamage() / (float) stack.getMaxDamage()) >= 0.5F && getData() != null && getData().hasKey("broken_texture") ? getData().getString("broken_texture") : this.getRegistryName().getPath();

        if (slot == EntityEquipmentSlot.CHEST && smallArms)
            tex = tex + "_smallarms";
        if (this.canOpenArmor(slot) && open)
            tex = tex + "_open";
        if (light)
            tex = tex + "_lights";

        return getModId() + ":textures/models/armor/" + name + "/" + tex + ".png";
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }
}
