package lucraft.mods.lucraftcore.extendedinventory.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.container.ContainerExtendedInventory;
import lucraft.mods.lucraftcore.tabs.TabRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiExtendedInventory extends GuiContainer {

    public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(LucraftCore.MODID, "textures/gui/extended_inventory.png");

    private float oldMouseX;
    private float oldMouseY;

    public GuiExtendedInventory(EntityPlayer player) {
        super(new ContainerExtendedInventory(player));
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = 176;
        this.ySize = 174;

        int i = this.guiLeft;
        int j = this.guiTop;

        TabRegistry.updateTabValues(i, j, InventoryTabExtendedInventory.class);
        TabRegistry.addTabsToList(this.buttonList);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.oldMouseX = (float) mouseX;
        this.oldMouseY = (float) mouseY;
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        InventoryExtendedInventory inv = ((ContainerExtendedInventory) this.inventorySlots).inventory2;

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_NECKLACE).isEmpty()) {
            this.drawTexturedModalRect(i + 114, j + 8, 208, 0, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE).isEmpty()) {
            this.drawTexturedModalRect(i + 114, j + 25, 192, 0, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_WRIST).isEmpty()) {
            this.drawTexturedModalRect(i + 114, j + 44, 176, 0, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_LEFT_WEAPON).isEmpty()) {
            this.drawTexturedModalRect(i + 17, j + 35, 224, 0, 16, 16);
        }

        if (inv.getStackInSlot(InventoryExtendedInventory.SLOT_RIGHT_WEAPON).isEmpty()) {
            this.drawTexturedModalRect(i + 142, j + 35, 240, 0, 16, 16);
        }

        GuiInventory.drawEntityOnScreen(i + 88, j + 75, 30, (float) (i + 51) - this.oldMouseX, (float) (j + 75 - 50) - this.oldMouseY, this.mc.player);
    }

}
