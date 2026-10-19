package lucraft.mods.heroes.speedsterheroes.items;

import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.items.ItemBase;
import lucraft.mods.lucraftcore.util.IArmorUpgrade;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.item.ItemStack;

public class ItemRingUpgrade extends ItemBase implements IArmorUpgrade {

	public ItemRingUpgrade() {
		super("ringUpgrade");
		
		LucraftCore.proxy.registerModel(this, new LCModelEntry(0, "ring"));
		setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
		SHItems.items.add(this);
	}

	@Override
	public boolean canBeApplied(int armorSlot, ItemStack suit, ItemStack upgrade) {
		return armorSlot == 1;
	}

}