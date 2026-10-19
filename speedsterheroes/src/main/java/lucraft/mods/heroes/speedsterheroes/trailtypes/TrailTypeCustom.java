package lucraft.mods.heroes.speedsterheroes.trailtypes;

import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRenderer;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

public class TrailTypeCustom extends TrailType {

	public EnumTrailType trail;
	public float secondaryRed;
	public float secondaryGreen;
	public float secondaryBlue;
	public boolean specialColors;
	public Random rand;
	public int playerLevel;
	public final EntityPlayer player;

	public TrailTypeCustom(NBTTagCompound nbt, EntityPlayer player) {
		super("");
		
		this.player = player;
		this.trail = EnumTrailType.getTrailTypeFromName(nbt.getString("TrailType"));
		this.setTrailColor(nbt.getFloat("PrimaryRed"), nbt.getFloat("PrimaryGreen"), nbt.getFloat("PrimaryBlue"));
		this.setMirageColor(nbt.getFloat("SecondaryRed"), nbt.getFloat("SecondaryGreen"), nbt.getFloat("SecondaryBlue"));
		this.setMiragesColorMasks(true, true, true);
		this.specialColors = nbt.getBoolean("SpecialColors");
		this.rand = new Random();
	}

	public String getDisplayName() {
		return LucraftCoreUtil.translateToLocal("speedsterheroes.trailtype.custom.name");
	}

	@Override
	public Vec3d getTrailColor() {
		return specialColors ? new Vec3d(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()) : super.getTrailColor();
	}

	@Override
	public Vec3d getMirageColor() {
		return specialColors ? new Vec3d(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()) : super.getMirageColor();
	}

	@Override
	public SpeedTrailRenderer getSpeedTrailRenderer() {
		int level = SuperpowerHandler.getSuperpowerPlayerHandler(player).getLevel();
		return trail.getRequiredLevel() > level ? EnumTrailType.NORMAL.getSpeedTrailRenderer() : trail.getSpeedTrailRenderer();
	}

}
