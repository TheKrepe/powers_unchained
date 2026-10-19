package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeComicQuicksilver extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	protected SpeedsterTypeComicQuicksilver() {
		super("comicQuicksilver", TrailType.particles_blue);
		this.setSpeedLevelRenderData(0, 36);
	}
	
	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("Comic Version");
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 7;
	}
	
	@Override
	public boolean shouldHideNameTag(EntityLivingBase player, boolean hasMaskOpen) {
		return false;
	}
	
	@Override
	public boolean hasSymbol() {
		return false;
	}

	@Override
	public boolean hasArmorOn(EntityLivingBase player) {
		if (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && !player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty() && !player.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty() && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == SHItems.comicQuicksilverChestplate && player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == SHItems.comicQuicksilverLegs && player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == SHItems.comicQuicksilverBoots) {
			return true;
		}
		return false;
	}
	
	@Override
	public List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return  SpeedsterHeroesUtil.getOresWithAmount("blockSilver", 1);
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.LIGHT_BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getLegs())
			return LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, 1);
		if(toRepair.getItem() == this.getBoots())
			return LCItems.getColoredTriPolymer(EnumDyeColor.WHITE, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.LIGHT_BLUE, 1);
	}

}
