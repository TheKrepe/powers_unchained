package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice.TachyonDeviceType;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SpeedLevelBarTachyonDevice extends SpeedLevelBarSpeedsterType {

	public TachyonDeviceType type;
	public ItemStack icon;
	
	public SpeedLevelBarTachyonDevice(String name, TachyonDeviceType type) {
		super(name);
		this.type = type;
		this.icon = new ItemStack(type == TachyonDeviceType.PROTOTYPE ? SHItems.tachyonPrototype : (type == TachyonDeviceType.DEVICE ? SHItems.tachyonDevice : (type == TachyonDeviceType.SMALL_DEVICE ? SHItems.smallTachyonDevice : null)));
	}
	
	@Override
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		super.drawIcon(mc, x, y, type);
		mc.getRenderItem().renderItemAndEffectIntoGUI(icon, x - 4, y);
	}

}
