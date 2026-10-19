package lucraft.mods.heroes.speedsterheroes.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LCModelEntry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemSHIcon extends Item {

	public ItemSHIcon() {
		String name = "iconItem";
		this.setUnlocalizedName(name);
		this.setRegistryName(name);
		GameRegistry.register(this);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		
		int iconAmount = 7;
		
		for(int i = 0; i < iconAmount; i++) {
			LucraftCore.proxy.registerModel(this, new LCModelEntry(i, "icons/" + i));
		}
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		
	}
}
