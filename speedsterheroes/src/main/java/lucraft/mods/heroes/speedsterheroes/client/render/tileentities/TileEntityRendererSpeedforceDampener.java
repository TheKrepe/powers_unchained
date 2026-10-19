package lucraft.mods.heroes.speedsterheroes.client.render.tileentities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntitySpeedforceDampener;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityRendererSpeedforceDampener extends TileEntitySpecialRenderer<TileEntitySpeedforceDampener> {

	public static ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/entity/speedforceDampener.png");

	@Override
	public void renderTileEntityAt(TileEntitySpeedforceDampener te, double x, double y, double z, float partialTicks, int destroyStage) {
		if(!SHConfig.speedforceDampenerRender)
			return;
		
		float progress = (te.timer + LucraftCoreClientUtil.renderTick) / 50F;
		boolean flag = te.getWorld().isBlockPowered(te.getPos());
		
		GlStateManager.pushMatrix();

		TrailType type = TrailType.lightnings_orange;

		float expand = 0.02F;
		AxisAlignedBB box = new AxisAlignedBB(te.getPos().getX() - expand, te.getPos().getY() - expand, te.getPos().getZ() - expand, te.getPos().getX() + 1 + expand, te.getPos().getY() + 1 + expand, te.getPos().getZ() + 1 + expand);
		box = box.expand(progress * 1.5F, progress * 1.5F, progress * 1.5F);
		if(flag)
			box = box.expand(-0.5F, -0.5F, -0.5F);
		
		type.getSpeedTrailRenderer().renderFlickering(box, box, type);

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
		GL11.glScalef(6.0F, 6.0F, 6.0F);

		GlStateManager.scale(progress, progress, progress);

		GlStateManager.disableLighting();
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(770, 771);
		GL11.glAlphaFunc(516, 0.003921569F);
		GL11.glDepthMask(true);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
		GL11.glColor4f(2.0F, 2.0F, 2.0F, (1F - progress) / 2F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glCallList(RenderDimensionBreach.sphereIdOutside);
		GL11.glCallList(RenderDimensionBreach.sphereIdInside);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

}
