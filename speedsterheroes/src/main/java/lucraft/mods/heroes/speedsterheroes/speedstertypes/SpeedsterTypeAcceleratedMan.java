package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityLightningThrowing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeAcceleratedMan extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	public SpeedsterTypeAcceleratedMan() {
		super("acceleratedMan", TrailType.lightnings_purple);
		this.setSpeedLevelRenderData(54, 21);
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
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == getBoots())
			return LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, 1);
		if(toRepair.getItem() == getHelmet())
			return LCItems.getColoredTriPolymer(EnumDyeColor.BROWN, 1);
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, 1);
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilityPhasing(player).setUnlocked(true).setRequiredLevel(15));
		list.add(new AbilityLightningThrowing(player).setUnlocked(true).setRequiredLevel(20));
		list.add(new AbilityTimeRemnant(player).setUnlocked(true).setRequiredLevel(25));
		list.add(new AbilityDimensionBreach(player).setUnlocked(true).setRequiredLevel(25));
		return super.addDefaultAbilities(player, list);
	}
	
	@Override
	public Ability getSuitAbilityForKey(LucraftKeys key, List<Ability> list) {
		if(key == LucraftKeys.ARMOR_1)
			return Ability.getAbilityFromClass(list, AbilityPhasing.class);
		if(key == LucraftKeys.ARMOR_2)
			return Ability.getAbilityFromClass(list, AbilityTimeRemnant.class);
		if(key == LucraftKeys.ARMOR_3)
			return Ability.getAbilityFromClass(list, AbilityDimensionBreach.class);
		return super.getSuitAbilityForKey(key, list);
	}

	@Override
	public List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		Item item = null;
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("paneGlass", 2);
		else if(armorSlot == EntityEquipmentSlot.CHEST)
			item = Items.LEATHER_CHESTPLATE;
		else if(armorSlot == EntityEquipmentSlot.LEGS)
			item = Items.LEATHER_LEGGINGS;
		else if(armorSlot == EntityEquipmentSlot.FEET)
			item = Items.LEATHER_BOOTS;
		return Arrays.asList(new ItemStack(item));
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("ingotGold", 2);
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.RED, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.BROWN, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.YELLOW, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public boolean hasSymbol() {
		return false;
	}

}
