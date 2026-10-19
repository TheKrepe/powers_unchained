package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityBoiler;
import lucraft.mods.lucraftcore.utilities.container.ContainerBoiler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class GuiBoiler extends GuiContainer {

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/boiler.png");

    private final InventoryPlayer playerInventory;
    private final TileEntityBoiler tileEntity;

    public GuiBoiler(EntityPlayer player, TileEntityBoiler tileEntity) {
        super(new ContainerBoiler(player, tileEntity));
        this.playerInventory = player.inventory;
        this.tileEntity = tileEntity;
        this.ySize = 216;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        String s = this.tileEntity.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

        EnergyUtil.drawTooltip(this.tileEntity.energyStorage.getEnergyStored(), this.tileEntity.energyStorage.getMaxEnergyStored(), this, 68, 97, 40, 12, mouseX - i, mouseY - j);
        LCFluidUtil.drawTooltip(this.tileEntity.fluidTankInput, this, 8, 38, 16, 60, mouseX - i, mouseY - j);
        LCFluidUtil.drawTooltip(this.tileEntity.fluidTankOutput, this, 152, 38, 16, 60, mouseX - i, mouseY - j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        IEnergyStorage storage = this.tileEntity.getCapability(CapabilityEnergy.ENERGY, null);
        float energy = (float) storage.getEnergyStored() / (float) storage.getMaxEnergyStored();
        this.drawTexturedModalRect(i + 68, j + 97, 200, 0, (int) (40 * energy), 12);

        LCRenderHelper.renderTiledFluid(i + 8, j + 38, 16, 0, this.tileEntity.fluidTankInput);
        LCRenderHelper.renderTiledFluid(i + 152, j + 38, 16, 0, this.tileEntity.fluidTankOutput);
        mc.renderEngine.bindTexture(GUI_TEXTURES);
        GlStateManager.color(1, 1, 1, 1);
        this.drawTexturedModalRect(i + 7, j + 37, 176, 17, 18, 62);
        this.drawTexturedModalRect(i + 151, j + 37, 176, 17, 18, 62);
        int l = (int) (((float) tileEntity.progress / 100F) * 24);
        this.drawTexturedModalRect(i + 108, j + 47, 176, 0, l + 1, 16);
    }
}
