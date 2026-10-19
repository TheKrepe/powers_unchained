package lucraft.mods.heroesexpansion.client.gui;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.container.ContainerPortalDevice;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessagePortalDevice;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.io.IOException;

public class GuiPortalDevice extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/portal_device.png");

    private final InventoryPlayer playerInventory;
    private final TileEntityPortalDevice tileEntity;

    public GuiPortalDevice(InventoryPlayer playerInventory, TileEntityPortalDevice tileEntity) {
        super(new ContainerPortalDevice(playerInventory, tileEntity));
        this.playerInventory = playerInventory;
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        this.addButton(new GuiButtonExt(0, i + 89, j + 18, 80, 18, StringHelper.translateToLocal(this.tileEntity.isPortalOpened() ? "heroesexpansion.info.close_portal" : "heroesexpansion.info.open_portal")));
        this.addButton(new GuiButtonExt(1, i + 89, j + 40, 80, 18, StringHelper.translateToLocal("heroesexpansion.info.set_destination")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            HEPacketDispatcher.sendToServer(new MessagePortalDevice(this.tileEntity.getPos()));
            tileEntity.opened = !tileEntity.opened;
            button.displayString = StringHelper.translateToLocal(this.tileEntity.isPortalOpened() ? "heroesexpansion.info.close_portal" : "heroesexpansion.info.open_portal");
        } else if (button.id == 1) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiOpenPortal(this.tileEntity));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileEntity.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

        TileEntityPortalDevice.EnumPortalDeviceDestination destination = tileEntity.destination;
        boolean unicode = this.fontRenderer.getUnicodeFlag();
        this.fontRenderer.setUnicodeFlag(true);

        if (destination == TileEntityPortalDevice.EnumPortalDeviceDestination.POSITION && tileEntity.destinationPos != null)
            this.fontRenderer.drawString(DimensionType.getById(tileEntity.destinationDim).getName().toUpperCase() + ": " + tileEntity.destinationPos.x + " - " + tileEntity.destinationPos.y + " - " + tileEntity.destinationPos.z, 30, 60, 4210752);

        else if (destination == TileEntityPortalDevice.EnumPortalDeviceDestination.INVASION)
            this.fontRenderer.drawString(StringHelper.translateToLocal("heroesexpansion.info.invasion"), 30, 60, 4210752);

        this.fontRenderer.setUnicodeFlag(unicode);

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        EnergyUtil.drawTooltip(this.tileEntity.getField(1), this.tileEntity.energyStorage.getMaxEnergyStored(), this, 10, 8, 12, 40, mouseX - i, mouseY - j);

        this.buttonList.get(0).enabled = this.tileEntity.destination != TileEntityPortalDevice.EnumPortalDeviceDestination.NONE && !this.tileEntity.getStackInSlot(1).isEmpty();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        int energy = (int) (((float) this.tileEntity.getField(1) / (float) this.tileEntity.energyStorage.getMaxEnergyStored()) * 40);
        drawTexturedModalRect(i + 10, j + 8 + 40 - energy, 176, 40 - energy, 12, energy);

        int progress = (int) (((float) this.tileEntity.getField(0) / (float) TileEntityPortalDevice.maxProgress) * 26F);
        drawTexturedModalRect(i + 43, j + 44, 188, 0, progress, 7);
    }
}
