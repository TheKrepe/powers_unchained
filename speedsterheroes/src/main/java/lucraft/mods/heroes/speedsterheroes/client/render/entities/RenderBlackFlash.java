package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

public class RenderBlackFlash extends RenderLivingBase<EntityBlackFlash> {

	public static ModelPlayer normalModel = new ModelPlayer(0.0F, false);
	public static ResourceLocation texture = new ResourceLocation(SpeedsterHeroes.MODID, "textures/entity/blackFlash.png");
	
	public RenderBlackFlash(RenderManager renderManager) {
		this(renderManager, false);
	}

	public RenderBlackFlash(RenderManager renderManager, boolean useSmallArms) {
		super(renderManager, normalModel, 0.5F);
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
	}

	public ModelPlayer getMainModel() {
		return (ModelPlayer) super.getMainModel();
	}

	public void doRender(EntityBlackFlash entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackFlash entity) {
		return texture;
	}

	public void transformHeldFull3DItemLayer() {
		GlStateManager.translate(0.0F, 0.1875F, 0.0F);
	}

	protected void preRenderCallback(EntityBlackFlash entity, float partialTickTime) {
		float f = 0.9375F;
		GlStateManager.scale(f, f, f);
	}

}
