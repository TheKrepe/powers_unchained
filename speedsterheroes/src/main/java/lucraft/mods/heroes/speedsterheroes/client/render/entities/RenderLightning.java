package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.client.render.speedtrail.SpeedTrailRendererLightnings;
import lucraft.mods.heroes.speedsterheroes.entity.EntityLightning;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLightning extends Render<EntityLightning> {

	public RenderLightning(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityLightning entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.blendFunc(770, 1);
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		EntityPlayer player = Minecraft.getMinecraft().player;
		double posX = -entity.posX - (player.posX - entity.posX);
		double posY = -entity.posY - (player.posY - entity.posY);
		double posZ = -entity.posZ - (player.posZ - entity.posZ);
		GlStateManager.translate(x, y, z);
//		System.out.println("" + (entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F));
		GlStateManager.rotate(entity.rotationYaw - 90F, 0.0F, 1.0F, 0.0F);
//		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

		SpeedsterType type = entity.type;
		if(type == null || type.getTrailType() == null)
			type = SpeedsterType.flashS2;
		
		if(type.getTrailType().getSpeedTrailRenderer() instanceof SpeedTrailRendererLightnings) {
			Random rand = new Random();
			Vec3d v1 = new Vec3d(0, rand.nextFloat() / 8, 0);
			Vec3d v2 = new Vec3d(-1F, rand.nextFloat() / 4, rand.nextFloat() / 4 - 0.0625F);
			Vec3d v3 = new Vec3d(-2F, rand.nextFloat() / 4, rand.nextFloat() / 4 - 0.0625F);
			Vec3d v4 = new Vec3d(-3F, rand.nextFloat() / 4, rand.nextFloat() / 4 - 0.0625F);
			Vec3d v5 = new Vec3d(-4F, rand.nextFloat() / 4, rand.nextFloat() / 4 - 0.0625F);
			
			((SpeedTrailRendererLightnings)type.getTrailType().getSpeedTrailRenderer()).drawLine(v1, v2, SpeedTrailRendererLightnings.lineWidth, SpeedTrailRendererLightnings.innerLineWidth, type.getTrailType(), 1F, null, null, 0);
			((SpeedTrailRendererLightnings)type.getTrailType().getSpeedTrailRenderer()).drawLine(v2, v3, SpeedTrailRendererLightnings.lineWidth, SpeedTrailRendererLightnings.innerLineWidth, type.getTrailType(), 1F, null, null, 0);
			((SpeedTrailRendererLightnings)type.getTrailType().getSpeedTrailRenderer()).drawLine(v3, v4, SpeedTrailRendererLightnings.lineWidth, SpeedTrailRendererLightnings.innerLineWidth, type.getTrailType(), 1F, null, null, 0);
			((SpeedTrailRendererLightnings)type.getTrailType().getSpeedTrailRenderer()).drawLine(v4, v5, SpeedTrailRendererLightnings.lineWidth, SpeedTrailRendererLightnings.innerLineWidth, type.getTrailType(), 1F, null, null, 0);
		}

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
		super.doRender(entity, posX, posY, posZ, entityYaw, partialTicks);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityLightning entity) {
		return null;
	}

}
