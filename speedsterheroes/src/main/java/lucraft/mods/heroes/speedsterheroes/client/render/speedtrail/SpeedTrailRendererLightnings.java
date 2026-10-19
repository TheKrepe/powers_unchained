package lucraft.mods.heroes.speedsterheroes.client.render.speedtrail;

import java.util.LinkedList;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
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

public class SpeedTrailRendererLightnings extends SpeedTrailRenderer {

	public static int lineWidth = 5;
	public static int innerLineWidth = 1;
	
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
			float lastBrightnessX = OpenGlHelper.lastBrightnessX;
			float lastBrightnessY = OpenGlHelper.lastBrightnessY;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			EntityPlayer player = Minecraft.getMinecraft().player;
			translateRendering(player, en);

			int amountOfLightnings = 6;
			float lightningSpace = en.height / amountOfLightnings;

			LinkedList<EntitySpeedMirage> list = SHRenderer.getSpeedMiragesFromPlayer(en);
			for (int j = 0; j < amountOfLightnings; j++) {
				// 10 Blitze ----------------------------------------------
				Vec3d add = new Vec3d(0, j * lightningSpace, 0);
				float differ = 0.425F * (en.height / 1.8F);
				if (list.size() > 0) {

					Vec3d firstStart = (((EntitySpeedMirage)list.getLast()).getLightningPosVector(j).subtract(((EntitySpeedMirage)list.getLast()).getPositionEyes(LucraftCoreClientUtil.renderTick))).add((en.getPositionEyes(LucraftCoreClientUtil.renderTick).addVector(0.0D, -1.62F * (en.height / 1.8F), 0.0D)));
					Vec3d firstEnd = list.getLast().getLightningPosVector(j);
					float a = 1F - (list.getLast().progress + LucraftCoreClientUtil.renderTick) / 10F;
					drawLine(firstStart.add(add).addVector(0, en.height, 0), firstEnd.add(add.addVector(0, list.getLast().lightningFactor[j] * differ, 0)), lineWidth, innerLineWidth, type, a, list.getLast().acquired, list.getLast(), j);
					
					for (int i = 0; i < list.size(); i++) {
						if (i < (list.size() - 1)) {
							EntitySpeedMirage speedMirage = list.get(i);
							EntitySpeedMirage speedMirage2 = list.get(i + 1);
							Vec3d start = speedMirage.getLightningPosVector(j);
							Vec3d end = speedMirage2.getLightningPosVector(j);
							float progress = 1F - (speedMirage.progress + LucraftCoreClientUtil.renderTick) / 10F;
							drawLine(start.add(add.addVector(0, speedMirage.lightningFactor[j] * differ, 0)), end.add(add.addVector(0, speedMirage2.lightningFactor[j] * differ, 0)), 5, 1, type, progress, speedMirage.acquired, speedMirage, j);
						}
					}
				}
			}
			GlStateManager.color(1, 1, 1, 1);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.popMatrix();
		}
	}
	
	@SideOnly(Side.CLIENT)
	public TrailType getTrailType(EntityLivingBase player, EntitySpeedMirage esm, TrailType origType, int lightningNumber) {
		if(SpeedsterHeroesUtil.isVelocity9Active(player)) {
			float remaining = SpeedsterHeroesUtil.getRemainingVelocity9Duration(player) / 20;
			float needed = 150;
			
			if(remaining <= needed) {
				float c = esm != null ? esm.lightningFactor[lightningNumber] : rand.nextFloat();
				float f = remaining / needed;
//				player.addChatMessage(new ChatComponentText("" + f));
				if(c > f)
					return TrailType.lightnings_blue;
			}
			
		}
		
		return origType;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFlickering(EntityLivingBase entity, TrailType type) {
		if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && (entity == Minecraft.getMinecraft().player))
			return;

		if(entity != Minecraft.getMinecraft().player) {
			this.renderFlickering(entity.getEntityBoundingBox(), getPrevPosAxisAlignedBox(entity), type);
			return;
		}
		
		GlStateManager.pushMatrix();
		float scale = entity.getCapability(LucraftCore.SIZECHANGE_CAP, null).getSize();
		GlStateManager.scale(1/scale, 1/scale, 1/scale);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 1);
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

		EntityPlayer player = Minecraft.getMinecraft().player;
		double x = -entity.posX - (player.posX - entity.posX);
		double y = -entity.posY - (player.posY - entity.posY);
		double z = -entity.posZ - (player.posZ - entity.posZ);
		GlStateManager.translate(x, y, z);

		for (int i = 0; i < 1; i++) {
			float f = entity.width / 2;
			float j = rand.nextFloat() * entity.height;
			drawLine(new Vec3d(entity.posX + rand.nextFloat() * entity.width - f, entity.posY + j, entity.posZ + rand.nextFloat() * entity.width - f), new Vec3d(entity.posX + rand.nextFloat() * entity.width - f, entity.posY + j, entity.posZ + rand.nextFloat() * entity.width - f), lineWidth, innerLineWidth, type, 1, player, null, 0);
		}

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFlickering(AxisAlignedBB box, AxisAlignedBB prevPosBox, TrailType type) {
//		if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && (playerIn == Minecraft.getMinecraft().thePlayer))
//			return;

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 1);
		float lastBrightnessX = OpenGlHelper.lastBrightnessX;
		float lastBrightnessY = OpenGlHelper.lastBrightnessY;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		EntityPlayer player = Minecraft.getMinecraft().player;
		
		double x = -median(box.minX, prevPosBox.minX) - (median(player.posX, player.prevPosX) - median(box.minX, prevPosBox.minX));
		double y = -median(box.minY, prevPosBox.minY) - (median(player.posY, player.prevPosY) - median(box.minY, prevPosBox.minY));
		double z = -median(box.minZ, prevPosBox.minZ) - (median(player.posZ, player.prevPosZ) - median(box.minZ, prevPosBox.minZ));
		GL11.glTranslatef((float) x + (float)(box.maxX - box.minX) / 2F, (float) y, (float) z + (float)(box.maxZ - box.minZ) / 2F);

		for (int i = 0; i < 1; i++) {
			float width = (float) Math.abs(box.maxX - box.minX);
			float f = width / 2;
			float j = (float) (rand.nextFloat() * Math.abs(box.maxY - box.minY));
			
			Vec3d start = new Vec3d(box.minX + rand.nextFloat() * width - f, box.minY + j, box.minZ + rand.nextFloat() * width - f);
			Vec3d end = new Vec3d(box.minX + rand.nextFloat() * width - f, box.minY + j, box.minZ + rand.nextFloat() * width - f);
			float distance = (float) start.distanceTo(end);
			Vec3d middle = new Vec3d((start.xCoord + end.xCoord) / 2F + (rand.nextFloat() - 0.5F) * distance, (start.yCoord + end.yCoord) / 2F + (rand.nextFloat() - 0.5F) * distance, (start.zCoord + end.zCoord) / 2F + (rand.nextFloat() - 0.5F) * distance);
			
			drawLine(start, middle, lineWidth, innerLineWidth, type, 1, null, null, 0);
			drawLine(middle, end, lineWidth, innerLineWidth, type, 1, null, null, 0);
		}

		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void preRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		GlStateManager.depthMask(true);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.colorMask(type.getMiragesColorMasks()[0], type.getMiragesColorMasks()[1], type.getMiragesColorMasks()[2], true);
		GlStateManager.color((float) type.getMirageColor().xCoord, (float) type.getMirageColor().yCoord, (float) type.getMirageColor().zCoord, 0.7F - entity.progress / 10F);
	}

	@SideOnly(Side.CLIENT)
	public void drawLine(Vec3d start, Vec3d end, float lineWidth, float innerLineWidth, TrailType type, float alpha, EntityLivingBase speedster, EntitySpeedMirage mirage, int lightningNumber) {
		if(start == null || end == null)
			return;
		
		if(speedster != null) {
			type = getTrailType(speedster, mirage, type, lightningNumber);
		}
		
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer wr = tes.getBuffer();

		if (lineWidth > 0) {
			GlStateManager.color((float) type.getTrailColor().xCoord, (float) type.getTrailColor().yCoord, (float) type.getTrailColor().zCoord, alpha);
			GL11.glLineWidth(lineWidth);
			wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			wr.pos(start.xCoord, start.yCoord, start.zCoord).endVertex();
			wr.pos(end.xCoord, end.yCoord, end.zCoord).endVertex();
			tes.draw();
		}

		if (innerLineWidth > 0) {
			GlStateManager.color(1, 1, 1, MathHelper.clamp(alpha - 0.2F, 0, 1));
			GL11.glLineWidth(innerLineWidth);
			wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			wr.pos(start.xCoord, start.yCoord, start.zCoord).endVertex();
			wr.pos(end.xCoord, end.yCoord, end.zCoord).endVertex();
			tes.draw();
		}
	}

	@SideOnly(Side.CLIENT)
	public static void drawInnerLight(Vec3d start, Vec3d end, EntityLivingBase entity, TrailType type, float alpha) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer wr = tes.getBuffer();

		GlStateManager.color((float) type.getMirageColor().xCoord, (float) type.getMirageColor().yCoord, (float) type.getMirageColor().zCoord, alpha);

		wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);

		wr.pos(start.xCoord, start.yCoord, start.zCoord);
		wr.pos(start.xCoord, start.yCoord + entity.height, start.zCoord);

		wr.pos(end.xCoord, end.yCoord, end.zCoord);
		wr.pos(end.xCoord, end.yCoord + entity.height, end.zCoord);

		wr.pos(end.xCoord, end.yCoord + entity.height, end.zCoord);
		wr.pos(end.xCoord, end.yCoord, end.zCoord);

		wr.pos(start.xCoord, start.yCoord + entity.height, start.zCoord);
		wr.pos(start.xCoord, start.yCoord, start.zCoord);

		tes.draw();
	}
  
	@Override
	public boolean shouldRenderSpeedMirage(EntitySpeedMirage entity, TrailType type) {
		return false;
	}
}