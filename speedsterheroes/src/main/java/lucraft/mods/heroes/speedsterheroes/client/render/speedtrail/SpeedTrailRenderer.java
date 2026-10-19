package lucraft.mods.heroes.speedsterheroes.client.render.speedtrail;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class SpeedTrailRenderer {

	Random rand = new Random();

	@SideOnly(Side.CLIENT)
	public abstract void renderTrail(EntityLivingBase entity, TrailType type);

	@SideOnly(Side.CLIENT)
	public abstract void renderFlickering(AxisAlignedBB box, AxisAlignedBB prevPosBox, TrailType type);

	@SideOnly(Side.CLIENT)
	public abstract void renderFlickering(EntityLivingBase entity, TrailType type);
	
	@SideOnly(Side.CLIENT)
	public abstract void preRenderSpeedMirage(EntitySpeedMirage entity, TrailType type);

	@SideOnly(Side.CLIENT)
	public abstract boolean shouldRenderSpeedMirage(EntitySpeedMirage entity, TrailType type);

	@SideOnly(Side.CLIENT)
	public float median(double currentPos, double prevPos) {
		return (float) (prevPos + (currentPos - prevPos) * LucraftCoreClientUtil.renderTick);
	}

	@SideOnly(Side.CLIENT)
	public void translateRendering(EntityPlayer player, Entity entity) {
		double x = -median(entity.posX, entity.prevPosX) - (median(player.posX, player.prevPosX) - median(entity.posX, entity.prevPosX));
		double y = -median(entity.posY, entity.prevPosY) - (median(player.posY, player.prevPosY) - median(entity.posY, entity.prevPosY));
		double z = -median(entity.posZ, entity.prevPosZ) - (median(player.posZ, player.prevPosZ) - median(entity.posZ, entity.prevPosZ));
		GL11.glTranslatef((float) x, (float) y, (float) z);
	}
	
	public AxisAlignedBB getPrevPosAxisAlignedBox(EntityLivingBase entity) {
		return new AxisAlignedBB(entity.prevPosX, entity.prevPosY, entity.prevPosZ, entity.prevPosX + entity.width, entity.prevPosY + entity.height, entity.prevPosZ + entity.width);
	}

}
