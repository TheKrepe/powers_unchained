package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.List;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IAutoSpeedsterRecipeAdvanced {

	public abstract List<ItemStack> getFirstItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
	public abstract List<ItemStack> getSecondItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
	public abstract List<ItemStack> getThirdItemStack(ItemStack armor, EntityEquipmentSlot armorSlot);
	
}
