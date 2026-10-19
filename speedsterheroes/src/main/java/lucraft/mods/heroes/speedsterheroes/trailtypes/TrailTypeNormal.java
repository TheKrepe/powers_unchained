package lucraft.mods.heroes.speedsterheroes.trailtypes;

import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRenderer;

public class TrailTypeNormal extends TrailType {

	public TrailTypeNormal() {
		super("normal");
		this.setTrailColor(1F, 1F, 1F);
		this.setMirageColor(1F, 1F, 1F);
		this.setMiragesColorMasks(true, true, true);
	}

	@Override
	public SpeedTrailRenderer getSpeedTrailRenderer() {
		return TrailType.renderer_normal;
	}
	
}
