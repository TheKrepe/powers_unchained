package lucraft.mods.heroes.speedsterheroes.items;

import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.lucraftcore.items.ItemBase;

public class ItemSHBase extends ItemBase {

	public ItemSHBase(String name) {
		super(name);
		this.setAutomaticModelName(name);
		setCreativeTab(SpeedsterHeroesProxy.tabSpeedster);
	}
	
}
