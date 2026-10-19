package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderDimensionBreach extends Render<EntityDimensionBreach> {

	public static ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/entity/dimensionBreach.png");

	public RenderDimensionBreach(RenderManager renderManager) {
		super(renderManager);

		Sphere sphere = new Sphere();
		sphere.setDrawStyle(GLU.GLU_FILL);
		sphere.setNormals(GLU.GLU_SMOOTH);
		sphere.setOrientation(GLU.GLU_OUTSIDE);

		sphereIdOutside = GL11.glGenLists(1);
		GL11.glNewList(sphereIdOutside, GL11.GL_COMPILE);
		// Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
		sphere.draw(0.5F, 32, 32);
		GL11.glEndList();

		sphere.setOrientation(GLU.GLU_INSIDE);
		sphereIdInside = GL11.glGenLists(1);
		GL11.glNewList(sphereIdInside, GL11.GL_COMPILE);
		// Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
		sphere.draw(0.5F, 32, 32);
		GL11.glEndList();
	}

	public static int sphereIdOutside;
	public static int sphereIdInside;

	@Override
	public void doRender(EntityDimensionBreach entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if(!SHConfig.breachRender)
			return;
		
		GL11.glPushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEX);
		GL11.glTranslated(x, y + entity.height / 2D, z);
		GL11.glScalef(3.0F, 3.0F, 3.0F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GlStateManager.disableLighting();
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

		float f = 1F + (float) Math.cos((entity.ticksExisted + LucraftCoreClientUtil.renderTick) * 0.1F) / 10F;
		GL11.glScalef(f, f, f);
		GL11.glTranslated(0, f - 1F, 0);

		GL11.glBlendFunc(770, 771);
		GL11.glAlphaFunc(516, 0.003921569F);

		float renderTick = entity.progress == 1 ? 0 : (entity.starting ? -partialTicks : partialTicks);
		float progress = 1F - (entity.progress + renderTick) / 20F;

		GlStateManager.scale(progress, progress, progress);

		GL11.glScalef(0.8F, 0.8F, 0.8F);
		GL11.glColor4f(2.0F, 2.0F, 2.0F, progress * 0.2F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glCallList(sphereIdOutside);
		GL11.glCallList(sphereIdInside);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);

		GL11.glScalef(1.4F, 1.4F, 1.4F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, progress * 0.5F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glCallList(sphereIdOutside);
		GL11.glCallList(sphereIdInside);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);

		GL11.glScalef(1.2F, 1.2F, 1.2F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, progress * 0.2F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glCallList(sphereIdOutside);
		GL11.glCallList(sphereIdInside);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);

		// GL11.glScalef(0.8F, 0.8F, 0.8F);
		// GL11.glColor4f(2.0F, 2.0F, 2.0F, 0.2F);
		// GL11.glEnable(GL11.GL_ALPHA_TEST);
		// GL11.glCallList(sphereIdOutside);
		// GL11.glCallList(sphereIdInside);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		//
		// GL11.glScalef(1.2F, 1.2F, 1.2F);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F);
		// GL11.glEnable(GL11.GL_ALPHA_TEST);
		// GL11.glCallList(sphereIdOutside);
		// GL11.glCallList(sphereIdInside);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		//
		// GL11.glScalef(0.83F, 0.83F, 0.83F);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
		// GL11.glEnable(GL11.GL_ALPHA_TEST);
		// GL11.glCallList(sphereIdOutside);
		// GL11.glCallList(sphereIdInside);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.enableLighting();
		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDimensionBreach entity) {
		return null;
	}

}
