package lucraft.mods.heroes.speedsterheroes.client.render.speedtrail;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedTrailRendererNormal extends SpeedTrailRenderer {

	@Override
	public void renderTrail(EntityLivingBase entity, TrailType type) {
		
	}

	@Override
	public void renderFlickering(AxisAlignedBB box, AxisAlignedBB prevPosBox, TrailType type) {
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		float progress = MathHelper.clamp(1F - (entity.progress + LucraftCoreClientUtil.renderTick) / 10F, 0F, 0.5F);
		float translate = -MathHelper.clamp(1F - (entity.progress) / 10F, 0F, 0.5F) / 15F;
		GlStateManager.translate(0F, translate * (entity.height / 1.8F), 0F);
		GL11.glBlendFunc(770, 771);
		GL11.glAlphaFunc(516, 0.003921569F);
		GlStateManager.color((float) type.getMirageColor().xCoord, (float) type.getMirageColor().yCoord, (float) type.getMirageColor().zCoord, progress);
		entity.alpha = progress;
	}

	@Override
	public boolean shouldRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFlickering(EntityLivingBase entity, TrailType type) {
		
	}

}
