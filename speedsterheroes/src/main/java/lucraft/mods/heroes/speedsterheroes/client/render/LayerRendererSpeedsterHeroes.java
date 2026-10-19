package lucraft.mods.heroes.speedsterheroes.client.render;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySpeedforceVision;
import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBarTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice.TachyonDeviceType;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.IUpgradableArmor;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class LayerRendererSpeedsterHeroes implements LayerRenderer<EntityPlayer> {

	public RenderLivingBase<?> renderer;

	public static Minecraft mc = Minecraft.getMinecraft();

	public LayerRendererSpeedsterHeroes(RenderLivingBase<?> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack tachyon = ItemStack.EMPTY;

		if (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof IUpgradableArmor && (LucraftCoreUtil.hasArmorThisUpgrade(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), SHItems.tachyonPrototype) || LucraftCoreUtil.hasArmorThisUpgrade(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), SHItems.tachyonDevice)))
			tachyon = SpeedsterHeroesUtil.getTachyonDeviceFromArmor(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
		else if (!player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE).isEmpty() && player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE).getItem() instanceof ItemTachyonDevice)
			tachyon = player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE);

		if (!tachyon.isEmpty()) {
			TachyonDeviceType tachyonType = ((ItemTachyonDevice) tachyon.getItem()).getTachyonDeviceType();
			if(tachyonType.getModel() == null)
				return;
			GlStateManager.pushMatrix();

			if (player.isSneaking()) {
				GlStateManager.translate(0, 0.18F, 0);
				// GlStateManager.rotate(30, 1, 0, 0);
			}
			((ModelBiped) renderer.getMainModel()).bipedBody.postRender(0.0625F);
			tachyonType.doModelTranslations(player, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
			Minecraft.getMinecraft().renderEngine.bindTexture(tachyonType.getTexture());
			tachyonType.getModel().render(null, 0, 0, 0, 0, 0, 0.0625F);

			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
			if (data != null && data.isInSpeed && SpeedsterHeroesUtil.getSpeedLevelList(player).get(data.speedLevel - 1) instanceof SpeedLevelBarTachyonDevice) {
				Tessellator tes = Tessellator.getInstance();
				VertexBuffer buf = tes.getBuffer();

				GL11.glDisable(3553);
				GL11.glDisable(2896);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
				GlStateManager.blendFunc(770, 1);

				if (tachyonType == TachyonDeviceType.PROTOTYPE) {
					GlStateManager.translate(0, 0, -0.1F);
					LCRenderHelper.setLightmapTextureCoords(240, 240);
					buf.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

					float alpha = ((MathHelper.sin((mc.player.ticksExisted + partialTicks) / 10F) + 1) / 4F) + 0.2F;

					buf.pos(-0.5F, 0F, 0).color(1, 0.5F, 0, alpha).endVertex();
					buf.pos(-0.5F, -0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.25F, -0.45F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0F, -0.45F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.25F, -0.45F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.5F, -0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.5F, 0F, 0).color(1, 0.5F, 0, alpha).endVertex();
					buf.pos(0.5F, 0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.25F, 0.45F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.25F, 0.45F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.5F, 0.25F, 0).color(0, 0, 0, 0).endVertex();

					tes.draw();

					LCRenderHelper.restoreLightmapTextureCoords();
				} else if (tachyonType == TachyonDeviceType.DEVICE) {
					// GlStateManager.translate(0, 0, -0.1F);
					LCRenderHelper.setLightmapTextureCoords(240, 240);
					buf.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);

					float alpha = ((MathHelper.sin((mc.player.ticksExisted + partialTicks) / 10F) + 1) / 6F) + 0.2F;

					buf.pos(-0.7F, 0F, 0).color(0.047F, 0.71F, 1, alpha).endVertex();
					buf.pos(-0.6F, -0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.35F, -0.6F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0F, -0.65F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.35F, -0.6F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.6F, -0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.7F, 0F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.6F, 0.25F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(0.35F, 0.6F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0F, 0.65F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.35F, 0.6F, 0).color(0, 0, 0, 0).endVertex();
					buf.pos(-0.6F, 0.25F, 0).color(0, 0, 0, 0).endVertex();

					tes.draw();

					LCRenderHelper.restoreLightmapTextureCoords();
				}

				GL11.glEnable(2896);
				GL11.glEnable(3553);
				GlStateManager.disableBlend();
			}

			GlStateManager.popMatrix();
		}

		if (player == mc.player)
			return;

		if (!SuperpowerHandler.hasSuperpower(mc.player) || SuperpowerHandler.getSuperpower(mc.player) != SpeedsterHeroes.speedforce)
			return;

		AbilitySpeedforceVision speedforceVision = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities(mc.player), AbilitySpeedforceVision.class);
		if (speedforceVision == null || !speedforceVision.isUnlocked() || !speedforceVision.isEnabled())
			return;

		if (!SuperpowerHandler.hasSuperpower(player) || SuperpowerHandler.getSuperpower(player) != SpeedsterHeroes.speedforce)
			return;

		SpeedforcePlayerHandler data1 = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		SpeedforcePlayerHandler data2 = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(mc.player, SpeedforcePlayerHandler.class);

		if (!data1.isInSpeed || !data2.isInSpeed)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.disableTexture2D();
		GlStateManager.disableDepth();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 0.0F, 0.0F, 0.5F);
		GlStateManager.blendFunc(770, 771);
		this.renderer.getMainModel().render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.blendFunc(771, 770);
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}

}
