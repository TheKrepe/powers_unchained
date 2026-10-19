package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelFalconWings;
import lucraft.mods.heroesexpansion.items.ItemSuitSetElytra;
import lucraft.mods.lucraftcore.superpowers.effects.EffectVibrating;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SuitSetFalcon extends HESuitSet {

    public NBTTagCompound data = new NBTTagCompound();

    public SuitSetFalcon(String name) {
        super(name);
        this.data.setBoolean("use_energy", true);
        this.data.setBoolean("elytra_particles", true);
        this.data.setString("wing_texture", HeroesExpansion.MODID + ":textures/models/armor/falcon/wings.png");
    }

    @Override
    public NBTTagCompound getData() {
        return data;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD;
    }

    @Override
    public ItemStack getRepairItem(ItemStack toRepair) {
        return new ItemStack(Items.IRON_INGOT);
    }

    @Override
    public ItemSuitSetArmor createItem(SuitSet suitSet, EntityEquipmentSlot slot) {
        if (slot == EntityEquipmentSlot.CHEST)
            return (ItemSuitSetArmor) new ItemSuitSetElytra(suitSet, EntityEquipmentSlot.CHEST).setRegistryName(suitSet.getRegistryName().getNamespace(), suitSet.getRegistryName().getPath() + "_chest");
        return super.createItem(suitSet, slot);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(SuitSet suitSet, ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        if (slot == EntityEquipmentSlot.CHEST) {
            ResourceLocation wings = suitSet.getData() != null && suitSet.getData().hasKey("wing_texture") ? new ResourceLocation(suitSet.getData().getString("wing_texture")) : null;
            return new ModelFalconWings(suitSet.getArmorModelScale(slot), suitSet.getArmorTexturePath(stack, entity, slot, false, smallArms, open), suitSet.getArmorTexturePath(stack, entity, slot, true, smallArms, open), suitSet, slot, smallArms, EffectVibrating.isVibrating(entity), wings);
        }
        return super.getArmorModel(suitSet, stack, entity, slot, light, smallArms, open);
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
}
