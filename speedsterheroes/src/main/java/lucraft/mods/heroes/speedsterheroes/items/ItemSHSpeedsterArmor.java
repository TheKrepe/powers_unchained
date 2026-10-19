package lucraft.mods.heroes.speedsterheroes.items;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemSHSpeedsterArmor extends ItemSpeedsterArmor {

	public ItemSHSpeedsterArmor(SpeedsterType type, EntityEquipmentSlot armorSlot) {
		super(type, armorSlot);
		
		this.itemName = type.getUnlocalizedName().toLowerCase() + getArmorSlotName(armorSlot);
		this.setUnlocalizedName(itemName);
		this.setRegistryName(itemName);
		GameRegistry.register(this);
		
		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, itemName));
	}
	
}
