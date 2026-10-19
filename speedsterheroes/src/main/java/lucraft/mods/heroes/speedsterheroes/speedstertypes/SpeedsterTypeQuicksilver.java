package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeQuicksilver extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	public SpeedsterTypeQuicksilver() {
		super("quicksilver", TrailType.particles_blue);
		this.setSpeedLevelRenderData(0, 36);
	}
	
	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("MCU Version");
	}
	
	@Override
	public boolean canOpenHelmet() {
		return false;
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 2;
	}
	
	@Override
	public float getArmorModelScale(EntityEquipmentSlot armorSlot) {
		return 0.29F;
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
	public List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return  SpeedsterHeroesUtil.getOresWithAmount("plateSilver", 1);
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.FEET)
			return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.SILVER, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.LIGHT_BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.FEET)
			return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.LIME, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.GRAY, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getLegs())
			return LCItems.getColoredTriPolymer(EnumDyeColor.GRAY, 1);
		if(toRepair.getItem() == this.getBoots())
			return LCItems.getColoredTriPolymer(EnumDyeColor.SILVER, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.LIGHT_BLUE, 1);
	}
	
}
