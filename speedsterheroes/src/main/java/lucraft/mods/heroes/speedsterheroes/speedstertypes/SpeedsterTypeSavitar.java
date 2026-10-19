package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityBlade;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityLightningThrowing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class SpeedsterTypeSavitar extends SpeedsterType {

	public SpeedsterTypeSavitar() {
		super("savitar", TrailType.lightnings_lightblue);
		this.setSpeedLevelRenderData(36, 21);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version");
	}

	@Override
	public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
		return true;
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 11;
	}

	@Override
	public boolean hasSymbol() {
		return false;
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilityPhasing(player).setUnlocked(true).setRequiredLevel(15));
		list.add(new AbilityLightningThrowing(player).setUnlocked(true).setRequiredLevel(20));
		list.add(new AbilityTimeRemnant(player).setUnlocked(true).setRequiredLevel(25));
		list.add(new AbilityDimensionBreach(player).setUnlocked(true).setRequiredLevel(25));
		list.add(new AbilityBlade(player).setUnlocked(true));
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
		if(key == LucraftKeys.ARMOR_4)
			return Ability.getAbilityFromClass(list, AbilityBlade.class);
		return super.getSuitAbilityForKey(key, list);
	}
	
}
