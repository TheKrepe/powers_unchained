package lucraft.mods.heroes.speedsterheroes.client.render;

import java.util.List;

import org.lwjgl.opengl.GL11;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBar;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpeedHUDRenderer {

	Minecraft mc = Minecraft.getMinecraft();
	public static ResourceLocation hudTex = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/gui/speedHud.png");
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent e) {
		if (e.isCancelable() || e.getType() != ElementType.EXPERIENCE) {
			return;
		}
		
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(mc.player, SpeedforcePlayerHandler.class);
		
		if (data != null && mc.player != null && (mc.currentScreen == null) && !mc.gameSettings.hideGUI && !mc.gameSettings.showDebugInfo && data.isInSpeed) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			List<SpeedLevelBar> list = SpeedsterHeroesUtil.getSpeedLevelList(mc.player);
			int speedPoints = list.size();
			int selectedPoint = data.speedLevel;
			SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(mc.player);
			int barWidth = (speedPoints * 15 - ((speedPoints - 1) * 3));
			int x = e.getResolution().getScaledWidth() / 2 - (barWidth / 2);
			int y = (int) (e.getResolution().getScaledHeight() / 1.4F);
			
			for (int i = 0; i < speedPoints; i++) {
				SpeedLevelBar speedLevelType = list.get(i);
				mc.renderEngine.bindTexture(type == null ? hudTex : new ResourceLocation(type.getSpeedLevelTextureLocation()));
				
				mc.ingameGUI.drawTexturedModalRect(x + i * 12, y, 0, 0, 15, 21);
				
				speedLevelType.drawIcon(mc, x + i * 12 + 3, y + 3, type);
				
				mc.renderEngine.bindTexture(hudTex);
				if (i == selectedPoint - 1)
					mc.ingameGUI.drawTexturedModalRect(x + i * 12 + 4, y + 18, 15, 17, 7, 4);
			}
			
			double d = round((SHConfig.useMph ? 0.621371F : 1D) * (mc.player.getDistance(mc.player.prevPosX, mc.player.prevPosY, mc.player.prevPosZ) * 20D * 3.6D), 2);
			String detail = LucraftCoreUtil.translateToLocal("speedsterheroes.info.speedlevel") + ": " + selectedPoint + " - " + (d < 1D ? 0 : d) + (SHConfig.useMph ? " mph" : " km/h");
			int stringLength = mc.fontRendererObj.getStringWidth(detail);
			LCRenderHelper.drawStringWithOutline(detail, e.getResolution().getScaledWidth() / 2 - stringLength / 2, y - 10, 0xffffff, 0);
			
			if(SpeedsterHeroesUtil.isVelocity9Active(mc.player) && mc.player.getActivePotionEffect(SpeedsterHeroesProxy.velocity9) != null) {
				String v9Timer = LucraftCoreUtil.translateToLocal("item.velocity9.name") + ": " + LucraftCoreUtil.intToTime(mc.player.getActivePotionEffect(SpeedsterHeroesProxy.velocity9).getDuration() / 20);
				int timerStringLength = mc.fontRendererObj.getStringWidth(v9Timer);
				LCRenderHelper.drawStringWithOutline(v9Timer, e.getResolution().getScaledWidth() / 2 - timerStringLength / 2, y + 25, 0xffffff, 0);
			}
			
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
			
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
