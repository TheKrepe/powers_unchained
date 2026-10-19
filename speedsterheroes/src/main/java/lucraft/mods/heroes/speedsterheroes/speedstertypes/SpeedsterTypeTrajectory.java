package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeTrajectory extends SpeedsterType implements IAutoSpeedsterRecipe {

	protected SpeedsterTypeTrajectory() {
		super("trajectory", TrailType.lightnings_orange);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version");
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 5;
	}
	
	@Override
	public ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SHItems.getSymbolFromSpeedsterType(this, 1);
	}

	@Override
	public ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}

	@Override
	public ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}

	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, 1);
	}
	
}
