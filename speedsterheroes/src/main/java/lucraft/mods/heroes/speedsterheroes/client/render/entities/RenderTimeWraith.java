package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeWraith;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderTimeWraith extends RenderLivingBase<EntityTimeWraith> {

	private static ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/entity/timeWraith.png");
	
	public RenderTimeWraith(RenderManager renderManager) {
		super(renderManager, new ModelPlayer(0F, false), 0.5F);
	}

	@Override
	public void doRender(EntityTimeWraith entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	@Override
	public void renderName(EntityTimeWraith entity, double x, double y, double z) {
		
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityTimeWraith entity) {
		return TEX;
	}

}
