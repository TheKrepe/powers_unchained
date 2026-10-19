package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeWallyWestComicKidFlash extends SpeedsterType implements IAutoSpeedsterRecipe {

	protected SpeedsterTypeWallyWestComicKidFlash() {
		super("wallyWestComicKidFlash", TrailType.lightnings_gold);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("Comic Version", "Kid Flash");
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 3;
	}
	
	@Override
	public boolean canBeCraftedDescription(ItemStack stack) {
		return false;
	}
	
	@Override
	public ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SHItems.getSymbolFromSpeedsterType(this, 1);
	}

	@Override
	public ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}

	@Override
	public ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getLegs())
			return LCItems.getColoredTriPolymer(EnumDyeColor.RED, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, 1);
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilityPhasing(player).setUnlocked(true).setRequiredLevel(15));
		list.add(new AbilityDimensionBreach(player).setUnlocked(true).setRequiredLevel(25));
		return super.addDefaultAbilities(player, list);
	}
	
	@Override
	public Ability getSuitAbilityForKey(LucraftKeys key, List<Ability> list) {
		if(key == LucraftKeys.ARMOR_1)
			return Ability.getAbilityFromClass(list, AbilityPhasing.class);
		if(key == LucraftKeys.ARMOR_3)
			return Ability.getAbilityFromClass(list, AbilityDimensionBreach.class);
		return super.getSuitAbilityForKey(key, list);
	}
	
}
