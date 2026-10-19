package lucraft.mods.heroes.speedsterheroes.client.gui;

import java.io.IOException;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToServer;
import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToServer.InfoType;
import lucraft.mods.heroes.speedsterheroes.network.SHPacketDispatcher;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.container.ContainerDummy;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiDimensionBreach extends GuiContainer {

	public SpeedforcePlayerHandler data;
	
	public GuiDimensionBreach(EntityPlayer player) {
		super(new ContainerDummy());
		data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
	}

	public static final ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/gui/dimensionBreach.png");
	
	public int xSize_ = 256;
	public int ySize_ = 189;
	
	public int selectedWaypoint;
	public GuiDimensionList list;
	
	@Override
	public void initGui() {
		super.initGui();
		this.xSize = xSize_;
		this.ySize = ySize_;

		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		this.buttonList.add(new GuiButtonExt(0, i + 20, j + 165, 68, 18, LucraftCoreUtil.translateToLocal("speedsterheroes.info.addwaypoint")));
		this.buttonList.add(new GuiButtonExt(1, i + 93, j + 165, 68, 18, LucraftCoreUtil.translateToLocal("selectWorld.delete")));
		this.buttonList.add(new GuiButtonExt(2, i + 166, j + 165, 68, 18, LucraftCoreUtil.translateToLocal("gui.cancel")));
		
		list = new GuiDimensionList(mc, this);
		this.selectedWaypoint = -1;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 2) {
			mc.player.closeScreen();
		}
		else if(button.id == 0) {
			mc.player.openGui(SpeedsterHeroes.instance, GuiIds.newWaypoint, mc.player.world, (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
		}
		else if(button.id == 1 && selectedWaypoint != -1) {
			SHPacketDispatcher.sendToServer(new MessageSendInfoToServer(InfoType.DELETE_WAYPOINT, selectedWaypoint));
			data.waypoints.remove(selectedWaypoint);
			if(data.chosenWaypointIndex == selectedWaypoint)
				data.chosenWaypointIndex = -1;
			else if(data.chosenWaypointIndex > selectedWaypoint)
				data.chosenWaypointIndex--;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1);
		
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		mc.getTextureManager().bindTexture(TEX);
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		
		String headline = LucraftCoreUtil.translateToLocal("speedsterheroes.abilities.dimensionBreach.name");
		int length = mc.fontRendererObj.getStringWidth(headline);
		LCRenderHelper.drawStringWithOutline(headline, i + xSize / 2 - length / 2, j + 12, 0xfefefe, 0);
		
		if(list != null)
			this.list.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onGuiClosed() {
		SHPacketDispatcher.sendToServer(new MessageSendInfoToServer(InfoType.DIMENSION_BREACH_CANCEL));
		super.onGuiClosed();
	}
}
