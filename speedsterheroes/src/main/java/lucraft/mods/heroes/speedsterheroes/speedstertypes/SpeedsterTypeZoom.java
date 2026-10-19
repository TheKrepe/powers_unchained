package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityLightningThrowing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeZoom extends SpeedsterType implements IAutoSpeedsterRecipe {

	protected SpeedsterTypeZoom() {
		super("zoom", TrailType.lightnings_blue);
		this.setSpeedLevelRenderData(18, 21);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version");
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 6;
	}
	
	@Override
	public boolean doesFlicker() {
		return true;
	}
	
	@Override
	public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
		return slot == EntityEquipmentSlot.HEAD && entity instanceof EntityPlayer && SuperpowerHandler.hasSuperpower((EntityPlayer) entity) && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer)entity, SpeedforcePlayerHandler.class).isInSpeed && SpeedsterHeroesUtil.hasArmorOn(entity);
	}
	
	@Override
	public ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return SHItems.getSymbolFromSpeedsterType(this, 1);
	}

	@Override
	public ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.BLACK, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}

	@Override
	public ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.BLACK, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		return LCItems.getColoredTriPolymer(EnumDyeColor.BLACK, 1);
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
	
}
