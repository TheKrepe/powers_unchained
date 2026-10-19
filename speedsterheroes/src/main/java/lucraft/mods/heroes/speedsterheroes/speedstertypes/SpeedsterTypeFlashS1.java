package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
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
import net.minecraft.item.ItemStack;

public class SpeedsterTypeFlashS1 extends SpeedsterType implements IAutoSpeedsterRecipe {

	protected SpeedsterTypeFlashS1() {
		super("flashS1", TrailType.lightnings_orange);
	}

	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version", LucraftCoreUtil.translateToLocal("speedsterheroes.info.season").replace("%SEASON", "1"));
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 3;
	}
	
	@Override
	public float getTachyonDeviceModelTranslation(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ModelAdvancedBiped model) {
		return 0.1F;
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
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilityPhasing(player).setUnlocked(true).setRequiredLevel(15));
		return super.addDefaultAbilities(player, list);
	}
	
	@Override
	public Ability getSuitAbilityForKey(LucraftKeys key, List<Ability> list) {
		if(key == LucraftKeys.ARMOR_1)
			return Ability.getAbilityFromClass(list, AbilityPhasing.class);
		return super.getSuitAbilityForKey(key, list);
	}
	
}
