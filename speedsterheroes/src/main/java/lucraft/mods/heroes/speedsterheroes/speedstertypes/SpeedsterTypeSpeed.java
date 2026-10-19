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

public class SpeedsterTypeSpeed extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	protected SpeedsterTypeSpeed() {
		super("speed", TrailType.particles_green);
		this.setSpeedLevelRenderData(9, 36);
	}

	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 2;
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
		return Arrays.asList(new ItemStack(Items.SUGAR));
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("paneGlassOrange", 2);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.GREEN, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("nuggetIron", 2);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.WHITE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getHelmet())
			return new ItemStack(Items.IRON_INGOT);
		return LCItems.getColoredTriPolymer(EnumDyeColor.GREEN, 1);
	}
	
}
