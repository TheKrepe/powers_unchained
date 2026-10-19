package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityLightningThrowing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.client.model.ModelAdvancedBiped;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeFlash extends SpeedsterType implements IAutoSpeedsterRecipe {

	protected SpeedsterTypeFlash() {
		super("flash", TrailType.lightnings_orange);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version", LucraftCoreUtil.translateToLocal("speedsterheroes.info.season").replace("%SEASON", "2"));
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 5;
	}
	
	@Override
	public float getTachyonDeviceModelTranslation(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ModelAdvancedBiped model) {
		return 0.1F;
	}

	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
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
	public ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SHItems.getSymbolFromSpeedsterType(this, 1);
	}

	@Override
	public ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		Item item = null;
		if(armorSlot == EntityEquipmentSlot.HEAD)
			item = SpeedsterType.flashS1.getHelmet();
		else if(armorSlot == EntityEquipmentSlot.CHEST)
			item = SpeedsterType.flashS1.getChestplate();
		else if(armorSlot == EntityEquipmentSlot.LEGS)
			item = SpeedsterType.flashS1.getLegs();
		else if(armorSlot == EntityEquipmentSlot.FEET)
			item = SpeedsterType.flashS1.getBoots();
		return new ItemStack(item);
	}

	@Override
	public ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}
	
}
