package lucraft.mods.heroes.speedsterheroes.items;

import java.util.ArrayList;

import com.google.common.collect.Multimap;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.IAutoSpeedsterRecipe;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.IAutoSpeedsterRecipeAdvanced;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.lucraftcore.suitset.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.util.IUpgradableArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

public class ItemSpeedsterArmor extends ItemSuitSetArmor implements ISpecialArmor, IUpgradableArmor {

	public static ArrayList<ItemSpeedsterArmor> armorToCraft = new ArrayList<ItemSpeedsterArmor>();
	
	public ItemSpeedsterArmor(SpeedsterType type, EntityEquipmentSlot armorSlot) {
		super(type, armorSlot);
		
		if(type instanceof IAutoSpeedsterRecipe || type instanceof IAutoSpeedsterRecipeAdvanced) {
			armorToCraft.add(this);
		}
		
		if(armorSlot == EntityEquipmentSlot.HEAD)
			type.helmet = new ItemStack(this);
		else if(armorSlot == EntityEquipmentSlot.CHEST)
			type.chestplate = new ItemStack(this);
		else if(armorSlot == EntityEquipmentSlot.LEGS)
			type.legs = new ItemStack(this);
		else if(armorSlot == EntityEquipmentSlot.FEET)
			type.boots = new ItemStack(this);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		return getSpeedsterType().getAttributeModifiers(slot, stack, super.getAttributeModifiers(slot, stack));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return ("" + I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name")).trim();
	}
	
	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		super.onCreated(stack, worldIn, playerIn);
		
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
	}

	public SpeedsterType getSpeedsterType() {
		return (SpeedsterType) getSuitSet();
	}
	
	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		EntityEquipmentSlot s = slot == 0 ? EntityEquipmentSlot.HEAD : (slot == 1 ? EntityEquipmentSlot.CHEST : (slot == 2 ? EntityEquipmentSlot.LEGS : EntityEquipmentSlot.FEET));
		float reduceAmount = ArmorMaterial.CHAIN.getDamageReductionAmount(s) + (armor.hasTagCompound() ? (armor.getTagCompound().getInteger(SHNBTTags.plateAmount) * 2) : 0);
		return new ArmorProperties(0, reduceAmount / 25D, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return (int)(getProperties(player, armor, null, 0D, slot).AbsorbRatio * 25D);
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		slot = 3 - slot;
		float d = damage / 4.0F;
		
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		d /= stack.getTagCompound().getInteger(SHNBTTags.plateAmount) + 1;
		
		if (d < 1.0F) {
			d = 1.0F;
		}

		stack.damageItem((int) d, entity);

		if (stack.getCount() == 0) {
			stack = ItemStack.EMPTY;
		}

	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return getSpeedsterType().getIsRepairable(toRepair, repair);
	}
	
}
