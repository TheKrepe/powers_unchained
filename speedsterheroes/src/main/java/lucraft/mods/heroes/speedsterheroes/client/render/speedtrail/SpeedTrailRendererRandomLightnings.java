package lucraft.mods.heroes.speedsterheroes.client.render.speedtrail;

import java.util.LinkedList;

import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedTrailRendererRandomLightnings extends SpeedTrailRendererLightnings {

	@Override
	@SideOnly(Side.CLIENT)
	public void renderTrail(EntityLivingBase en, TrailType type) {
		if (SHRenderer.canHaveTrail(en)) {
			if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0 && (en == Minecraft.getMinecraft().player || (en instanceof EntitySpeedMirage && ((EntitySpeedMirage) en).acquired == Minecraft.getMinecraft().player)))
				return;
			
			TrailType.renderer_lightnings.renderTrail(en, type);
			TrailType.renderer_particles.renderTrail(en, type);
			
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
			translateRendering(player, en);
			
//			int lineWidth = 10;
//			int innerLineWidth = 7;
			
			LinkedList<EntitySpeedMirage> list = SHRenderer.getSpeedMiragesFromPlayer(en);
			int radius = 6;
			float f = player.width / 2F;
			
			for(int i = 0; i < 20; i++) {
				if(i < list.size() - 1) {
					EntitySpeedMirage mirage = list.get(i);
					
					Vec3d start = en.getPositionVector().addVector(mirage.lightningFactor[0] * en.width - f, mirage.lightningFactor[0] * en.height, mirage.lightningFactor[0] * en.width - f);
					Vec3d end = new Vec3d(en.posX + f + rand.nextInt(radius) - radius / 2, en.posY + rand.nextFloat() * en.height, en.posZ + f + rand.nextInt(radius) - radius / 2);
					Vec3d middle = new Vec3d((start.xCoord + end.xCoord) / 2F + rand.nextFloat() - 0.5F, (start.yCoord + end.yCoord) / 2F + rand.nextFloat() - 0.5F, (start.zCoord + end.zCoord) / 2F + rand.nextFloat() - 0.5F);
					Vec3d quarter = new Vec3d((start.xCoord + middle.xCoord) / 2F + rand.nextFloat() - 0.5F, (start.yCoord + middle.yCoord) / 2F + rand.nextFloat() - 0.5F, (start.zCoord + middle.zCoord) / 2F + rand.nextFloat() - 0.5F);
					Vec3d thirdQuarter = new Vec3d((end.xCoord + middle.xCoord) / 2F + rand.nextFloat() - 0.5F, (end.yCoord + middle.yCoord) / 2F + rand.nextFloat() - 0.5F, (end.zCoord + middle.zCoord) / 2F + rand.nextFloat() - 0.5F);
					
					BlockPos posEnd = new BlockPos(end.xCoord, end.yCoord, end.zCoord);
					boolean isBlock = !player.world.isAirBlock(posEnd);
					if(isBlock) {
						drawLine(start, quarter, lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						drawLine(quarter, middle, lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						drawLine(middle, thirdQuarter, lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						drawLine(thirdQuarter, end, lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						
						drawLine(middle, new Vec3d(middle.xCoord + (rand.nextFloat() - 0.5F), middle.yCoord + (rand.nextFloat() - 0.5F), middle.zCoord + (rand.nextFloat() - 0.5F)), lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						drawLine(quarter, new Vec3d(quarter.xCoord + (rand.nextFloat() - 0.5F), quarter.yCoord + (rand.nextFloat() - 0.5F), quarter.zCoord + (rand.nextFloat() - 0.5F)), lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						drawLine(thirdQuarter, new Vec3d(thirdQuarter.xCoord + (rand.nextFloat() - 0.5F), thirdQuarter.yCoord + (rand.nextFloat() - 0.5F), thirdQuarter.zCoord + (rand.nextFloat() - 0.5F)), lineWidth, innerLineWidth, type, 1F, player, list.get(i), i);
						
						player.world.spawnParticle(EnumParticleTypes.FLAME, end.xCoord, end.yCoord, end.zCoord, 0F, 0.01F, 0F);
						player.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, end.xCoord, end.yCoord, end.zCoord, 0F, 0.1F, 0F);
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
	
}
