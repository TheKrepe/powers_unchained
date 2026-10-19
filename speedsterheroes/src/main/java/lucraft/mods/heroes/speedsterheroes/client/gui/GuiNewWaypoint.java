package lucraft.mods.heroes.speedsterheroes.client.gui;

import java.io.IOException;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.network.MessageNewWaypoint;
import lucraft.mods.heroes.speedsterheroes.network.SHPacketDispatcher;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.container.ContainerDummy;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class GuiNewWaypoint extends GuiContainer {

	public static final ResourceLocation TEX = new ResourceLocation(SpeedsterHeroes.ASSETDIR + "textures/gui/newWaypoint.png");
	
	public int xSize_ = 256;
	public int ySize_ = 105;
	public GuiTextField textField;
	
	public GuiNewWaypoint() {
		super(new ContainerDummy());
	}

	@Override
	public void initGui() {
		super.initGui();
		this.xSize = xSize_;
		this.ySize = ySize_;

		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		this.textField = new GuiTextField(0, fontRendererObj, i + 30, j + 40, 196, 20);
		this.textField.setText("Waypoint Name");
		this.textField.setFocused(true);
		
		this.buttonList.add(new GuiButtonExt(1, i + 10, j + 80, 50, 18, LucraftCoreUtil.translateToLocal("speedsterheroes.info.save")));
		this.buttonList.add(new GuiButtonExt(2, i + 195, j + 80, 50, 18, LucraftCoreUtil.translateToLocal("gui.back")));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button.id == 1) {
			TeleportDestination td = new TeleportDestination(this.textField.getText(), mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.dimension);
			SHPacketDispatcher.sendToServer(new MessageNewWaypoint(td));
		} else if(button.id == 2) {
			mc.player.openGui(SpeedsterHeroes.instance, GuiIds.dimensionBreach, mc.player.world, (int)mc.player.posX, (int)mc.player.posY, (int)mc.player.posZ);
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(!this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
			super.keyTyped(typedChar, keyCode);
		this.textField.textboxKeyTyped(typedChar, keyCode);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		this.textField.updateCursorCounter();
		
		this.buttonList.get(0).enabled = !textField.getText().isEmpty();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.textField.drawTextBox();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.textField.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1);
		
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		mc.getTextureManager().bindTexture(TEX);
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		
		LCRenderHelper.drawStringWithOutline(LucraftCoreUtil.translateToLocal("speedsterheroes.info.addwaypoint"), i + 10, j + 10, 0xffffff, 0);
	}

}
