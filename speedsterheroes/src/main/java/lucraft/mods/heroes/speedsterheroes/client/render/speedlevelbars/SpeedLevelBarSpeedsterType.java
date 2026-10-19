package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;

public class SpeedLevelBarSpeedsterType extends SpeedLevelBar {

	public SpeedLevelBarSpeedsterType(String name) {
		super(name);
	}
	
	@Override
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		mc.renderEngine.bindTexture(hudTex);
		mc.ingameGUI.drawTexturedModalRect(x, y, type == null ? 37 : (int) type.getSpeedLevelRenderData()[0], type == null ? 0 : (int) type.getSpeedLevelRenderData()[1], 9, 15);
	}

}
