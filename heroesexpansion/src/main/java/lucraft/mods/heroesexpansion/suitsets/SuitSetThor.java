package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.heroesexpansion.abilities.AbilityGodMode;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.RegistryEvent;

public class SuitSetThor extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();

    public SuitSetThor(String name) {
        super(name);
        this.data.setFloat("god_of_thunder_multiplier", 2F);
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return ItemArmor.ArmorMaterial.DIAMOND;
    }

    @Override
    public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
        if (this == HESuitSet.THOR_IW && slot == EntityEquipmentSlot.CHEST && entity instanceof EntityPlayer) {
            for (AbilityGodMode ab : Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityGodMode.class)) {
                if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(chestplate = createItem(this, EntityEquipmentSlot.CHEST));
        e.getRegistry().register(legs = createItem(this, EntityEquipmentSlot.LEGS));
        e.getRegistry().register(boots = createItem(this, EntityEquipmentSlot.FEET));
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(Items.IRON_INGOT);
    }

    @Override
    public NBTTagCompound getData() {
        return this.data;
    }
}
