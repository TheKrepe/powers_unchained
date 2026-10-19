package lucraft.mods.lucraftcore.infinity.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.blocks.TileEntityInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.container.ContainerInfinityGenerator;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.util.energy.EnergyUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class GuiInfinityGenerator extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/infinity_generator.png");

    public TileEntityInfinityGenerator tileEntity;
    public InventoryPlayer inventoryPlayer;

    public GuiInfinityGenerator(InventoryPlayer inventory, TileEntityInfinityGenerator tileEntity) {
        super(new ContainerInfinityGenerator(inventory, tileEntity));
        this.tileEntity = tileEntity;
        this.inventoryPlayer = inventory;
        this.ySize = 145;
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
        IItemHandler itemHandler = this.tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!itemHandler.getStackInSlot(0).isEmpty() && itemHandler.getStackInSlot(0).getItem() instanceof ItemInfinityStone) {
            ItemInfinityStone item = (ItemInfinityStone) itemHandler.getStackInSlot(0).getItem();
            this.fontRenderer.drawString(item.getEnergyPerTick(itemHandler.getStackInSlot(0)) + " FE/t", 65, 28, 4210752);
        }

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
    }
}
