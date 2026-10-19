package lucraft.mods.heroes.speedsterheroes.client;

import lucraft.mods.heroes.speedsterheroes.client.render.LayerRendererSpeedsterHeroes;
import lucraft.mods.lucraftcore.events.InitRenderArmorEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SHClientEvents {

	public static Minecraft mc = Minecraft.getMinecraft();

	public SHClientEvents() {
	}

	@SubscribeEvent
	public void onInitRenderArmor(InitRenderArmorEvent e) {
		e.getRenderArmor().addLayer(new LayerRendererSpeedsterHeroes(e.getRenderArmor()));
	}

}
