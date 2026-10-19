package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;

public class SpeedLevelBarBase extends SpeedLevelBarSpeedsterType {

	public SpeedLevelBarBase(String name) {
		super(name);
	}
	
	@Override
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		super.drawIcon(mc, x, y, type);
		mc.ingameGUI.drawTexturedModalRect(x, y, 28, 0, 9, 15);
	}

}
