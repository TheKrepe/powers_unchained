package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.Arrays;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.items.LCItems;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SpeedsterTypeJayGarrick extends SpeedsterType implements IAutoSpeedsterRecipeAdvanced {

	protected SpeedsterTypeJayGarrick() {
		super("jayGarrick", TrailType.lightnings_orange);
	}
	
	@Override
	public List<String> getExtraDescription(ItemStack stack, EntityPlayer player) {
		return Arrays.asList("CW Version", "Hunter Zolomon");
	}
	
	public boolean canOpenHelmet() {
		return false;
	}
	
	@Override
	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return 4;
	}
	
	@Override
	public boolean shouldHideNameTag(EntityLivingBase player, boolean hasMaskOpen) {
		return false;
	}
	
	@Override
	public List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		return Arrays.asList(SHItems.getSymbolFromSpeedsterType(this, 1));
	}

	@Override
	public List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("plateIron", 4);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.RED, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}

	@Override
	public List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot) {
		if(armorSlot == EntityEquipmentSlot.HEAD)
			return SpeedsterHeroesUtil.getOresWithAmount("ingotGold", 2);
		
		return Arrays.asList(LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, SpeedsterHeroesUtil.getRecommendedAmountForArmorSlotWithAddition(armorSlot, this)));
	}
	
	@Override
	public ItemStack getRepairItem(ItemStack toRepair) {
		if(toRepair.getItem() == this.getLegs()) {
			return LCItems.getColoredTriPolymer(EnumDyeColor.BLUE, 1);
		} else if(toRepair.getItem() == this.getHelmet()) {
			return OreDictionary.getOres("plateIron").get(0);
		}
		return LCItems.getColoredTriPolymer(EnumDyeColor.RED, 1);
	}
	
	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if(toRepair.getItem() == this.getHelmet()) {
			boolean hasFoundPlate = false;
			for(ItemStack stack : OreDictionary.getOres("plateIron")) {
				if(repair.getItem() == stack.getItem() && repair.getItemDamage() == stack.getItemDamage()) {
					hasFoundPlate = true;
					return true;
				}
			}
			return hasFoundPlate;
		} else {
			return super.getIsRepairable(toRepair, repair);
		}
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		return SpeedsterType.zoom.addDefaultAbilities(player, list);
	}
	
	@Override
	public Ability getSuitAbilityForKey(LucraftKeys key, List<Ability> list) {
		return SpeedsterType.zoom.getSuitAbilityForKey(key, list);
	}
	
}
