package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityExtractor;
import lucraft.mods.lucraftcore.utilities.container.ContainerExtractor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiExtractor extends GuiContainer {

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/extractor.png");
    private final InventoryPlayer playerInventory;
    private final TileEntityExtractor tileEntity;

    public GuiExtractor(EntityPlayer player, TileEntityExtractor tileEntity) {
        super(new ContainerExtractor(player, tileEntity));
        this.playerInventory = player.inventory;
        this.tileEntity = tileEntity;
        this.ySize = 174;
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
        EnergyUtil.drawTooltip(this.tileEntity.energyStorage.getEnergyStored(), this.tileEntity.energyStorage.getMaxEnergyStored(), this, 8, 27, 12, 40, mouseX - i, mouseY - j);
        LCFluidUtil.drawTooltip(this.tileEntity.fluidTank, this, 27, 17, 16, 60, mouseX - i, mouseY - j);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        int l = (int) (((float) tileEntity.progress / 100F) * 40);
        this.drawTexturedModalRect(i + 77, j + 36, 176, 0, l, 23);
        int energy = (int) (((float) this.tileEntity.energyStorage.getEnergyStored() / (float) this.tileEntity.energyStorage.getMaxEnergyStored()) * 40);
        drawTexturedModalRect(i + 8, j + 27 + 40 - energy, 194, 23 + 40 - energy, 12, energy);
        LCRenderHelper.renderTiledFluid(i + 27, j + 17, 16, 0, this.tileEntity.fluidTank);
        mc.renderEngine.bindTexture(GUI_TEXTURES);
        GlStateManager.color(1, 1, 1, 1);
        this.drawTexturedModalRect(i + 26, j + 16, 176, 23, 18, 62);
    }
}