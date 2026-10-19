package lucraft.mods.heroes.speedsterheroes.client.render.entities;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.client.models.ModelSpeedMirage;
import lucraft.mods.heroes.speedsterheroes.client.render.LayerSpeedMirageBipedArmor;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderSpeedMirage extends RenderLivingBase<EntitySpeedMirage> {

	public float alpha;
	
	public RenderSpeedMirage(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelSpeedMirage(0F, false), 0F);
		this.addLayer(new LayerSpeedMirageBipedArmor(this));
//		this.addLayer(new LayerHeldItem(this));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void doRender(EntitySpeedMirage entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.acquired == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
			return;
		}

		if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre(entity, this, x, y, z)))
			return;
		partialTicks = 1F;
		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		EntityPlayer player = null;
		
		if(entity.acquired instanceof EntityPlayer)
			player = (EntityPlayer) entity.acquired;
		else if(entity.acquired instanceof EntityTimeRemnant)
			player = (EntityPlayer) ((EntityTimeRemnant)entity.acquired).getOwner();
		
		String skinType = player instanceof AbstractClientPlayer ? ((AbstractClientPlayer) player).getSkinType() : "default";
		this.mainModel = new ModelSpeedMirage(0F, skinType.equalsIgnoreCase("slim"));

		float abc = entity.swingProgress - entity.prevSwingProgress;
		this.mainModel.swingProgress = entity.prevSwingProgress + (abc < 0.0F ? abc++ : abc) * partialTicks;
		boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());
		this.mainModel.isRiding = shouldSit;
		this.mainModel.isChild = entity.isChild();
		ModelSpeedMirage model = (ModelSpeedMirage) this.mainModel;
		model.bipedBodyWear.showModel = player.isWearing(EnumPlayerModelParts.JACKET);
		model.bipedHeadwear.showModel = player.isWearing(EnumPlayerModelParts.HAT);
		model.bipedLeftArmwear.showModel = player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
		model.bipedRightArmwear.showModel = player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
		model.bipedRightLegwear.showModel = player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
		model.bipedLeftLegwear.showModel = player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);

		this.alpha = entity.alpha;
		
		try {
			float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
			float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
			float f2 = f1 - f;

			if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase) {
				EntityLivingBase entitylivingbase = (EntityLivingBase) entity.getRidingEntity();
				f = this.interpolateRotation(entitylivingbase.prevRenderYawOffset, entitylivingbase.renderYawOffset, partialTicks);
				f2 = f1 - f;
				float f3 = MathHelper.wrapDegrees(f2);

				if (f3 < -85.0F) {
					f3 = -85.0F;
				}

				if (f3 >= 85.0F) {
					f3 = 85.0F;
				}

				f = f1 - f3;

				if (f3 * f3 > 2500.0F) {
					f += f3 * 0.2F;
				}
			}

			float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
			this.renderLivingAt(entity, x, y, z);
			float f8 = this.handleRotationFloat(entity, partialTicks);
			this.applyRotations(entity, f8, f, partialTicks);
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(-1.0F, -1.0F, 1.0F);
			this.preRenderCallback(entity, partialTicks);
			GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
			GlStateManager.translate(0.0F, 0.1F, 0.0F);
			float f5 = entity.prevLimbSwingAmount + (entity.limbSwingAmount - entity.prevLimbSwingAmount) * partialTicks;
			float f6 = entity.limbSwing - entity.limbSwingAmount * (1.0F - partialTicks);

			if (entity.isChild()) {
				f6 *= 3.0F;
			}

			if (f5 > 1.0F) {
				f5 = 1.0F;
			}

			GlStateManager.enableAlpha();
			this.mainModel.setLivingAnimations(entity, f6, f5, partialTicks);
			this.mainModel.setRotationAngles(f6, f5, f8, f2, f7, 0.0625F, entity);

			{
				boolean flag = this.setDoRenderBrightness(entity, partialTicks);

				float scale = entity.height / 1.8F;
				GlStateManager.translate(0F, -1.400494382F * scale + 1.400494382F, 0F);
				GlStateManager.scale(scale, scale, scale);
				this.renderModel(entity, f6, f5, f8, f2, f7, 0.0625F);
				this.renderLayers(entity, f6, f5, partialTicks, f8, f2, f7, 0.0625F);

				if (flag) {
					this.unsetBrightness();
				}

				GlStateManager.depthMask(true);

			}

			GlStateManager.disableRescaleNormal();
		} catch (Exception exception) {
		}

		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.colorMask(true, true, true, true);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GlStateManager.popMatrix();
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post(entity, this, x, y, z));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpeedMirage entity) {
		if (entity.acquired instanceof AbstractClientPlayer) {
			return ((AbstractClientPlayer) entity.acquired).getLocationSkin();
		}
		return null;
	}

	@Override
	public ModelBase getMainModel() {
		return super.getMainModel();
	}

}