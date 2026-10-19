package lucraft.mods.heroes.speedsterheroes.client.render.speedtrail;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedTrailRendererParticles extends SpeedTrailRenderer {

	@Override
	@SideOnly(Side.CLIENT)
	public void renderTrail(EntityLivingBase en, TrailType type) {
		if (SHRenderer.canHaveTrail(en)) {
			if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && (en == Minecraft.getMinecraft().player || (en instanceof EntitySpeedMirage && ((EntitySpeedMirage) en).acquired == Minecraft.getMinecraft().player)))
				return;

			GlStateManager.pushMatrix();
			float scale = en.getCapability(LucraftCore.SIZECHANGE_CAP, null).getSize();
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 1);
			EntityPlayer player = Minecraft.getMinecraft().player;
			translateRendering(player, en);

			int amountOfLightnings = 7;
			float lightningSpace = en.height / amountOfLightnings;

			LinkedList<EntitySpeedMirage> list = SHRenderer.getSpeedMiragesFromPlayer(en);
			
			if (list.size() > 0) {
				for (int j = 0; j < amountOfLightnings; j++) {
					// 10 Blitze ----------------------------------------------
					Vec3d add = new Vec3d(0, j * lightningSpace, 0);

					Vec3d firstStart = (((EntitySpeedMirage)list.getLast()).getLightningPosVector(j).subtract(((EntitySpeedMirage)list.getLast()).getPositionEyes(LucraftCoreClientUtil.renderTick))).add((en.getPositionEyes(LucraftCoreClientUtil.renderTick).addVector(0.0D, -1.62F * (en.height / 1.8F), 0.0D)));
					Vec3d firstEnd = list.getLast().getLightningPosVector(j);
					float a = 1F - (list.getLast().progress + LucraftCoreClientUtil.renderTick) / 10F;
					drawLine(firstStart.add(add.addVector(0, en.height, 0)), firstEnd.add(add), 2, 1, type, list.getLast().lightningFactor[j] < 0.5F ? type.getTrailColor() : new Vec3d(0.6F, 0.6F, 0.6F), a);

					for (int i = 0; i < list.size(); i++) {
						if (i < (list.size() - 1)) {
							EntitySpeedMirage speedMirage = list.get(i);
							EntitySpeedMirage speedMirage2 = list.get(i + 1);
							Vec3d start = speedMirage.getLightningPosVector(j);
							Vec3d end = speedMirage2.getLightningPosVector(j);
							float progress = 1F - (speedMirage.progress + LucraftCoreClientUtil.renderTick) / 10F;
							drawLine(start.add(add), end.add(add), 2, 1, type, speedMirage.lightningFactor[j] < 0.5F ? type.getTrailColor() : new Vec3d(0.6F, 0.6F, 0.6F), progress);
						}
					}
				}

				float a = 1F - (list.getLast().progress + LucraftCoreClientUtil.renderTick) / 10F;
				Vec3d firstStart = (((EntitySpeedMirage)list.getLast()).getLightningPosVector(0).subtract(((EntitySpeedMirage)list.getLast()).getPositionEyes(LucraftCoreClientUtil.renderTick))).add((en.getPositionEyes(LucraftCoreClientUtil.renderTick).addVector(0.0D, -1.62F * (en.height / 1.8F), 0.0D)));
				drawInnerLight(firstStart.addVector(0, en.height, 0), list.get(list.size() - 1).getPositionVector(), en.height, type, a);
				
				for (int i = 0; i < list.size(); i++) {
					if (i < (list.size() - 1)) {
						EntitySpeedMirage speedMirage = list.get(i);
						EntitySpeedMirage speedMirage2 = list.get(i + 1);
						float progress = 1F - (speedMirage.progress + LucraftCoreClientUtil.renderTick) / 10F;
						drawInnerLight(speedMirage.getPositionVector(), speedMirage2.getPositionVector(), speedMirage.height, type, progress);
					}
				}
			}
			
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFlickering(EntityLivingBase entity, TrailType type) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFlickering(AxisAlignedBB box, AxisAlignedBB prevPosBox, TrailType type) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		float progress = MathHelper.clamp(1F - (entity.progress + LucraftCoreClientUtil.renderTick) / 10F, 0F, 0.2F);
		float translate = -MathHelper.clamp(1F - (entity.progress) / 10F, 0F, 0.5F) / 15F;
		float scale = entity.height / 1.8F;
		GlStateManager.translate(0F, translate * scale, 0F);
		GL11.glBlendFunc(770, 771);
		GL11.glAlphaFunc(516, 0.003921569F);
		GlStateManager.color((float) type.getMirageColor().xCoord, (float) type.getMirageColor().yCoord, (float) type.getMirageColor().zCoord, progress);
		entity.alpha = progress;
	}

	@SideOnly(Side.CLIENT)
	public static void drawLine(Vec3d start, Vec3d end, float lineWidth, float innerLineWidth, TrailType type, Vec3d color, float alpha) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer wr = tes.getBuffer();
		
		for(int i = 0; i < 2; i++) {
			if (lineWidth > 0) {
				GlStateManager.pushMatrix();
				float translate = i > 0 ? i == 1 ? -0.025F : 0.025F : 0F;
				float a = i == 0 ? alpha : MathHelper.clamp(alpha - 0.2F, 0F, 1F);
				Vec3d c = i == 0 ? color : (color.subtract(type.getTrailColor()).lengthVector() != 0D ? color : new Vec3d(0.6F, 0.6F, 0.6F)); 
				GlStateManager.translate(translate, translate, translate);
				GlStateManager.color((float) c.xCoord, (float) c.yCoord, (float) c.zCoord, a);
				GL11.glLineWidth(lineWidth + translate);
				wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
				wr.pos(start.xCoord, start.yCoord, start.zCoord).endVertex();
				wr.pos(end.xCoord, end.yCoord, end.zCoord).endVertex();
				tes.draw();
				GlStateManager.popMatrix();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void drawInnerLight(Vec3d start, Vec3d end, float height, TrailType type, float alpha) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer wr = tes.getBuffer();
		
		float a = MathHelper.clamp(alpha, 0F, 0.25F);
		GlStateManager.color((float) type.getTrailColor().xCoord, (float) type.getTrailColor().yCoord, (float) type.getTrailColor().zCoord, a);

		wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);

		wr.pos(start.xCoord, start.yCoord, start.zCoord).endVertex();
		wr.pos(start.xCoord, start.yCoord + height, start.zCoord).endVertex();
		wr.pos(end.xCoord, end.yCoord + height, end.zCoord).endVertex();
		wr.pos(end.xCoord, end.yCoord, end.zCoord).endVertex();

		wr.pos(end.xCoord, end.yCoord, end.zCoord).endVertex();
		wr.pos(end.xCoord, end.yCoord + height, end.zCoord).endVertex();
		wr.pos(start.xCoord, start.yCoord + height, start.zCoord).endVertex();
		wr.pos(start.xCoord, start.yCoord, start.zCoord).endVertex();

		tes.draw();
	}

	@Override
	public boolean shouldRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		return true;
	}
}
