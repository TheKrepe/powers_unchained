package lucraft.mods.heroes.speedsterheroes.client.gui;

import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToServer;
import lucraft.mods.heroes.speedsterheroes.network.MessageSendInfoToServer.InfoType;
import lucraft.mods.heroes.speedsterheroes.network.SHPacketDispatcher;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiDimensionList extends GuiScrollingList {

	private GuiDimensionBreach parent;
	
	public GuiDimensionList(Minecraft client, GuiDimensionBreach parent) {
		super(client, 214, 140, ((parent.height - parent.ySize_) / 2) + 32, ((parent.height - parent.ySize_) / 2) + 21 + 140, ((parent.width - parent.xSize_) / 2) + 21, 27, parent.width, parent.height);
		this.parent = parent;
	}

	@Override
	protected int getSize() {
		return parent.data.waypoints.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
		parent.selectedWaypoint = index;
		parent.mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 1, 1);
		
		if(doubleClick) {
			parent.data.chosenWaypointIndex = index;
			SHPacketDispatcher.sendToServer(new MessageSendInfoToServer(InfoType.CHOSEN_WAYPOINT, index));
		}
	}

	@Override
	protected boolean isSelected(int index) {
		return index == parent.selectedWaypoint;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
		TeleportDestination td = parent.data.waypoints.get(slotIdx);
		
		LCRenderHelper.drawStringWithOutline(td.getName(), left + 10, slotTop + 3, 0xffffff, 0);
		
		boolean unicode = Minecraft.getMinecraft().fontRendererObj.getUnicodeFlag();
		parent.mc.fontRendererObj.setUnicodeFlag(true);
		LCRenderHelper.drawString(TextFormatting.GRAY + "X: " + td.getX() + " - Y: " + td.getY() + " - Z: " + td.getZ() + " - D: " + td.getDimensionId(), left + 10, slotTop + 12);
		Minecraft.getMinecraft().fontRendererObj.setUnicodeFlag(unicode);
		
		if(slotIdx == parent.data.chosenWaypointIndex) {
			parent.mc.renderEngine.bindTexture(GuiDimensionBreach.TEX);
			parent.drawTexturedModalRect(left + 190, slotTop + 5, 0, 189, 14, 12);
		}
	}
	
}
