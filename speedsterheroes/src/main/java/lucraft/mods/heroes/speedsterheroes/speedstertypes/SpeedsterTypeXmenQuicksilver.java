package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeXmenQuicksilver extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	protected SpeedsterTypeXmenQuicksilver() {
		super("xmenQuicksilver", TrailType.normal);
		this.setSpeedLevelRenderData(18, 36);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("X-Men Movie Version");
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
	public List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SpeedsterHeroesUtil.getOresWithAmount("blockSilver", 1);
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("paneGlassLightGray", 2);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.SILVER, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("nuggetIron", 2);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.LIGHT_BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getHelmet())
			return new ItemStack(Items.IRON_INGOT);
		if(toRepair.getItem() == this.getLegs())
			return LCItems.getColoredTriPolymer(EnumDyeColor.GRAY, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.SILVER, 1);
	}
	
}
