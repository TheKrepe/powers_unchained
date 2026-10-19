package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;

public class SpeedLevelBarExtraSpeed extends SpeedLevelBarSpeedsterType {

	public SpeedLevelBarExtraSpeed(String name) {
		super(name);
	}
	
	@Override
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		super.drawIcon(mc, x, y, type);
		mc.ingameGUI.drawTexturedModalRect(x + 2, y + 5, 22, 14, 6, 7);
	}

}
