package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IAutoSpeedsterRecipe {

	public abstract ItemStack getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
	public abstract ItemStack getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
	public abstract ItemStack getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
}
