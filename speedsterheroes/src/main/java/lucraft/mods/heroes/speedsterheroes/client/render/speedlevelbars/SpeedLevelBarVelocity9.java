package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SpeedLevelBarVelocity9 extends SpeedLevelBarSpeedsterType {

	public SpeedLevelBarVelocity9(String name) {
		super(name);
	}

	@Override
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		super.drawIcon(mc, x, y, type);
		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(SHItems.velocity9), x - 3, y);
	}
	
}
