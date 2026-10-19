package lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice.TachyonDeviceType;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedLevelBar {

	public static SpeedLevelBar baseSpeed = new SpeedLevelBarBase("baseSpeed");
	public static SpeedLevelBar velocity9Speed = new SpeedLevelBarVelocity9("velocity9Speed");
	public static SpeedLevelBar extraSpeed = new SpeedLevelBarExtraSpeed("extraSpeed");
	public static SpeedLevelBar speedsterTypeLevel = new SpeedLevelBarSpeedsterType("speedsterTypeLevel");
	public static SpeedLevelBar tachyonPrototypeSpeed = new SpeedLevelBarTachyonDevice("tachyonPrototypeSpeed", TachyonDeviceType.PROTOTYPE);
	public static SpeedLevelBar tachyonDeviceSpeed = new SpeedLevelBarTachyonDevice("tachyonDeviceSpeed", TachyonDeviceType.DEVICE);
	public static SpeedLevelBar smallTachyonDeviceSpeed = new SpeedLevelBarTachyonDevice("smallTachyonDeviceSpeed", TachyonDeviceType.SMALL_DEVICE);
	
	// -------------------------------------------------
	
	private String name;
	public static ResourceLocation hudTex = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/gui/speedHud.png");
	
	public SpeedLevelBar(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@SideOnly(Side.CLIENT)
	public void drawIcon(Minecraft mc, int x, int y, SpeedsterType type) {
		mc.renderEngine.bindTexture(hudTex);
	}
	
}
