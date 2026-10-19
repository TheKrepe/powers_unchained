package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntitySuitStand;
import lucraft.mods.lucraftcore.utilities.container.ContainerSuitStand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSuitStand extends GuiContainer {

    private static final ResourceLocation SUIT_STAND_GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/suit_stand.png");

    private final TileEntitySuitStand suitStand;
    private final InventoryPlayer playerInventory;
    /**
     * The old x position of the mouse pointer
     */
    private float oldMouseX;
    /**
     * The old y position of the mouse pointer
     */
    private float oldMouseY;

    public GuiSuitStand(EntityPlayer player, TileEntitySuitStand suitStand) {
        super(new ContainerSuitStand(player, suitStand));
        this.suitStand = suitStand;
        this.playerInventory = player.inventory;
        this.xSize = 176;
        this.ySize = 206;
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

        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.suitStand.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(SUIT_STAND_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        InventoryExtendedInventory inv = mc.player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

        // TE
        if (suitStand.getStackInSlot(4).isEmpty()) {
            this.drawTexturedModalRect(i + 89, j + 93, 208, 16, 16, 16);
        }

        if (suitStand.getStackInSlot(5).isEmpty()) {
            this.drawTexturedModalRect(i + 107, j + 93, 192, 16, 16, 16);
        }

        if (suitStand.getStackInSlot(6).isEmpty()) {
            this.drawTexturedModalRect(i + 125, j + 93, 176, 16, 16, 16);
        }

        // Player
        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_NECKLACE).isEmpty()) {
            this.drawTexturedModalRect(i + 26, j + 93, 208, 16, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE).isEmpty()) {
            this.drawTexturedModalRect(i + 44, j + 93, 192, 16, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_WRIST).isEmpty()) {
            this.drawTexturedModalRect(i + 62, j + 93, 176, 16, 16, 16);
        }

        GuiInventory.drawEntityOnScreen(i + 51, j + 83, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, this.mc.player);

    }
}
