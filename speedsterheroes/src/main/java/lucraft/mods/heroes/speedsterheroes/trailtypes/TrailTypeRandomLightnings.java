package lucraft.mods.heroes.speedsterheroes.trailtypes;

import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TrailTypeRandomLightnings extends TrailType {

	public TrailTypeRandomLightnings(String name) {
		super(name);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SpeedTrailRenderer getSpeedTrailRenderer() {
		return TrailType.renderer_electricity;
	}

}
