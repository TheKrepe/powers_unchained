package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.client.model.ModelAdvancedBiped;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeComicJesseQuick extends SpeedsterType implements IAutoSpeedsterRecipe{

	protected SpeedsterTypeComicJesseQuick() {
		super("comicJesseQuick", TrailType.lightnings_gold);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("Comic Version");
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 4;
	}
	
	@Override
	public float getTachyonDeviceModelTranslation(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ModelAdvancedBiped model) {
		return 0.1F;
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getLegs())
			return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, 1);
	}

	@Override
	public ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SHItems.getSymbolFromSpeedsterType(this, 1);
	}

	@Override
	public ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}

	@Override
	public ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}
	
}
