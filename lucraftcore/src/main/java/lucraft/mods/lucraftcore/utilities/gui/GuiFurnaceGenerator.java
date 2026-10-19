package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityFurnaceGenerator;
import lucraft.mods.lucraftcore.utilities.container.ContainerFurnaceGenerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFurnaceGenerator extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/furnace_generator.png");

    public TileEntityFurnaceGenerator tileEntity;
    public InventoryPlayer inventoryPlayer;

    public GuiFurnaceGenerator(InventoryPlayer inventory, TileEntityFurnaceGenerator tileEntity) {
        super(new ContainerFurnaceGenerator(inventory, tileEntity));
        this.tileEntity = tileEntity;
        this.inventoryPlayer = inventory;
        this.ySize = 145;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileEntity.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.inventoryPlayer.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        EnergyUtil.drawTooltip(this.tileEntity.energyStorage, this, 10, 8, 12, 40, mouseX - i, mouseY - j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        int energy = (int) (((float) this.tileEntity.energyStorage.getEnergyStored() / (float) this.tileEntity.energyStorage.getMaxEnergyStored()) * 40);
        drawTexturedModalRect(i + 10, j + 8 + 40 - energy, 176, 40 - energy, 12, energy);

        int fuel = this.tileEntity.fuel;
        if (fuel > 0) {
            int k = this.getBurnLeftScaled(13, fuel);
            this.drawTexturedModalRect(i + 62, j + 15 + 12 - k, 188, 12 - k, 14, k + 1);
        }
    }

    private int getBurnLeftScaled(int pixels, int fuel) {
        return fuel * pixels / this.tileEntity.totalFuel;
    }

}
