package lucraft.mods.heroes.speedsterheroes.trailtypes;

import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRenderer;
import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRendererLightnings;
import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRendererNormal;
import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRendererParticles;
import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRendererRandomLightnings;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TrailType {

	public static SpeedTrailRenderer renderer_normal = new SpeedTrailRendererNormal();
	public static SpeedTrailRenderer renderer_lightnings = new SpeedTrailRendererLightnings();
	public static SpeedTrailRenderer renderer_electricity = new SpeedTrailRendererRandomLightnings();
	public static SpeedTrailRenderer renderer_particles = new SpeedTrailRendererParticles();
	
	public static TrailType lightnings_orange = new TrailType("lightnings_orange").setTrailColor(1F, 0.3F, 0F).setMirageColor(1F, 0.2F, 0.2F);
	public static TrailType lightnings_red = new TrailType("lightnings_red").setTrailColor(1F, 0F, 0F).setMirageColor(0.5F, 0.5F, 0.0F);
	public static TrailType lightnings_blue = new TrailType("lightnings_blue").setTrailColor(0F, 0F, 1F).setMirageColor(0.3F, 0.3F, 1F);
	public static TrailType lightnings_gold = new TrailType("lightnings_gold").setTrailColor(1F, 1F, 0.13F).setMirageColor(0.3F, 0.3F, 1F);
	public static TrailType lightnings_brightgold = new TrailType("lightnings_brightgold").setTrailColor(1F, 0.75F, 0F).setMirageColor(0.3F, 0.3F, 1F);
	public static TrailType lightnings_white = new TrailType("lightnings_white").setTrailColor(1F, 1F, 1F);
	public static TrailType lightnings_lightblue = new TrailType("lightnings_lightblue").setTrailColor(0.28F, 1F, 1F);
	public static TrailType lightnings_purple = new TrailType("lightnings_purple").setTrailColor(0.78F, 0.36F, 0.78F);
	public static TrailType randomlightnings_lightblue = new TrailTypeRandomLightnings("randomlightnings_lightblue").setTrailColor(0.4F, 0.4F, 1F).setMirageColor(0.3F, 0.3F, 1F);
	public static TrailType particles_blue = new TrailTypeParticles("particles_blue").setTrailColor(0F, 0F, 1F).setMirageColor(0.3F, 0.3F, 1F);
	public static TrailType particles_green = new TrailTypeParticles("particles_green").setTrailColor(0F, 1F, 0F).setMirageColor(0F, 0F, 0F);
	public static TrailType normal = new TrailTypeNormal();
	
	//--------------------------------------------------------------------------
	
	protected String name;
	protected Vec3d trailColor;
	protected Vec3d mirageColor;
	protected boolean[] miragesColorMasks;
	
	public TrailType(String name) {
		this.name = name;
		trailColor = new Vec3d(1F, 0.3F, 0F);
		mirageColor = new Vec3d(1F, 0.2F, 0.2F);
		miragesColorMasks = new boolean[] {false, true, true};
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return LucraftCoreUtil.translateToLocal("speedsterheroes.trailtype." + name + ".name");
	}
	
	public TrailType setTrailColor(float r, float g, float b) {
		this.trailColor = new Vec3d(r, g, b);
		return this;
	}
	
	public Vec3d getTrailColor() {
		return trailColor;
	}
	
	public TrailType setMirageColor(float r, float g, float b) {
		this.mirageColor = new Vec3d(r, g, b);
		return this;
	}
	
	public Vec3d getMirageColor() {
		return mirageColor;
	}
	
	public TrailType setMiragesColorMasks(boolean par1, boolean par2, boolean par3) {
		this.miragesColorMasks = new boolean[] {par1, par2, par3};
		return this;
	}
	
	public boolean[] getMiragesColorMasks() {
		return miragesColorMasks;
	}
	
	@SideOnly(Side.CLIENT)
	public SpeedTrailRenderer getSpeedTrailRenderer() {
		return TrailType.renderer_lightnings;
	}
	
	public static enum EnumTrailType {
		
		NORMAL("speedsterheroes.trailtype.normal.name", 5, TrailType.renderer_normal), 
		PARTICLES("speedsterheroes.trailtype.particles.name", 15, TrailType.renderer_particles), 
		ELECTRICITY("speedsterheroes.trailtype.randomlightnings.name", 20, TrailType.renderer_electricity), 
		LIGHTNINGS("speedsterheroes.trailtype.lightnings.name", 25, TrailType.renderer_lightnings);
		
		private String name;
		private int requiredLevel;
		private SpeedTrailRenderer renderer;
		
		private EnumTrailType(String name, int requiredLevel, SpeedTrailRenderer renderer) {
			this.name = name;
			this.requiredLevel = requiredLevel;
			this.renderer = renderer;
		}
		
		public String getName() {
			return name;
		}
		
		public int getRequiredLevel() {
			return requiredLevel;
		}
		
		public SpeedTrailRenderer getSpeedTrailRenderer() {
			return renderer;
		}
		
		public static EnumTrailType getTrailTypeFromName(String name) {
			for(EnumTrailType types : values()) {
				if(types.toString().equalsIgnoreCase(name)) {
					return types;
				}
			}
			return NORMAL;
		}
		
	}
	
}
