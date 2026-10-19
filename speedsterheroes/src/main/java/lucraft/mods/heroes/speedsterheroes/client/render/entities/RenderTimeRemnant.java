package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;

public class RenderTimeRemnant extends RenderLivingBase<EntityTimeRemnant> {

	public static ModelPlayer normalModel = new ModelPlayer(0.0F, false);
	public static ModelPlayer smallArmsModel = new ModelPlayer(0.0F, true);
	
	public RenderTimeRemnant(RenderManager renderManager) {
		this(renderManager, false);
	}

	public RenderTimeRemnant(RenderManager renderManager, boolean useSmallArms) {
		super(renderManager, normalModel, 0.5F);
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
	}

	public ModelPlayer getMainModel() {
		return (ModelPlayer) super.getMainModel();
	}

	public void doRender(EntityTimeRemnant entity, double x, double y, double z, float entityYaw, float partialTicks) {
		double d0 = y;
		EntityPlayer clientPlayer = (EntityPlayer) entity.getOwner();
		if (clientPlayer == null)
			return;

		if(LucraftCoreClientUtil.hasSmallArms(clientPlayer))
			this.mainModel = smallArmsModel;
		else
			this.mainModel = normalModel;

		this.setModelVisibilities(entity);
		super.doRender(entity, x, d0, z, entityYaw, partialTicks);
	}

	private void setModelVisibilities(EntityTimeRemnant entity) {
		EntityPlayer clientPlayer = (EntityPlayer) entity.getOwner();
		ModelPlayer modelplayer = this.getMainModel();

		if (clientPlayer == null)
			return;

		if (clientPlayer.isSpectator()) {
			modelplayer.setInvisible(false);
			modelplayer.bipedHead.showModel = true;
			modelplayer.bipedHeadwear.showModel = true;
		} else {
			modelplayer.setInvisible(true);
			modelplayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
			modelplayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
			modelplayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
			modelplayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
			modelplayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
			modelplayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
			modelplayer.isSneak = false;
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTimeRemnant entity) {
		if (entity.getOwner() instanceof AbstractClientPlayer) {
			return ((AbstractClientPlayer) entity.getOwner()).getLocationSkin();
		} else
			return null;
	}

	public void transformHeldFull3DItemLayer() {
		GlStateManager.translate(0.0F, 0.1875F, 0.0F);
	}

	protected void preRenderCallback(EntityTimeRemnant entity, float partialTickTime) {
		float f = 0.9375F;
		GlStateManager.scale(f, f, f);
	}

}
