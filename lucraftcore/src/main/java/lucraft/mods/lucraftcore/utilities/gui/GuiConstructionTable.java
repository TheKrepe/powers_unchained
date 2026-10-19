package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityConstructionTable;
import lucraft.mods.lucraftcore.utilities.container.ContainerConstructionTable;
import lucraft.mods.lucraftcore.utilities.network.MessageSetSelectedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;

public class GuiConstructionTable extends GuiContainer {

    private static final ResourceLocation CONSTRUCTION_TABLE_GUI_TEXTURES = new ResourceLocation(LucraftCore.MODID, "textures/gui/construction_table.png");
    private final InventoryPlayer playerInventory;
    public final TileEntityConstructionTable tileConstructionTable;
    public GuiInstructionRecipeList recipeList;
    public GuiInstructionRequirementsList requirementsList;
    public int mouseX;
    public int mouseY;

    public GuiConstructionTable(EntityPlayer player, TileEntityConstructionTable tableInv) {
        super(new ContainerConstructionTable(player, tableInv));
        this.playerInventory = player.inventory;
        this.tileConstructionTable = tableInv;
    }

    @Override
    public void initGui() {
        super.initGui();

        recipeList = new GuiInstructionRecipeList(mc, this);
        requirementsList = new GuiInstructionRequirementsList(mc, this);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.tileConstructionTable.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CONSTRUCTION_TABLE_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        if (recipeList != null)
            this.recipeList.drawScreen(mouseX, mouseY, partialTicks);
        if (requirementsList != null)
            this.requirementsList.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static class GuiInstructionRecipeList extends GuiScrollingList {

        public GuiConstructionTable parent;

        public GuiInstructionRecipeList(Minecraft client, GuiConstructionTable parent) {
            super(client, 66, 51, ((parent.height - parent.getYSize()) / 2) + 17, ((parent.height - parent.getYSize()) / 2) + 17 + 51, ((parent.width - parent.getXSize()) / 2) + 37, 10, parent.width, parent.height);
            this.parent = parent;
        }

        @Override
        protected int getSize() {
            return parent.tileConstructionTable == null || parent.tileConstructionTable.getInstructionRecipes() == null ? 0 : parent.tileConstructionTable.getInstructionRecipes().size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {
            parent.mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 1, 1);
            LCPacketDispatcher.sendToServer(new MessageSetSelectedRecipe(parent.tileConstructionTable.getPos(), index));
        }

        @Override
        protected boolean isSelected(int index) {
            return index == parent.tileConstructionTable.selectedRecipe;
        }

        @Override
        protected void drawBackground() {

        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            boolean unicode = parent.fontRenderer.getUnicodeFlag();
            parent.fontRenderer.setUnicodeFlag(true);
            String s = parent.tileConstructionTable.getInstructionRecipes().get(slotIdx).getOutput().getDisplayName();
            boolean tooLong = false;
            while (parent.fontRenderer.getStringWidth(s) > 50) {
                s = s.substring(0, s.length() - 1);
                tooLong = true;
            }
            if (tooLong)
                s = s + "...";
            parent.fontRenderer.drawString(s, this.left + 2, slotTop - 1, 0xfefefe);
            parent.fontRenderer.setUnicodeFlag(unicode);
        }

    }

    public static class GuiInstructionRequirementsList extends GuiScrollingList {

        public GuiConstructionTable parent;

        public GuiInstructionRequirementsList(Minecraft client, GuiConstructionTable parent) {
            super(client, 30, 51, ((parent.height - parent.getYSize()) / 2) + 17, ((parent.height - parent.getYSize()) / 2) + 17 + 51, ((parent.width - parent.getXSize()) / 2) + 109, 20, parent.width, parent.height);
            this.parent = parent;
        }

        @Override
        protected int getSize() {
            if (parent.tileConstructionTable.selectedRecipe < 0)
                return 0;
            return parent.tileConstructionTable.getInstructionRecipes().get(parent.tileConstructionTable.selectedRecipe).getRequirements().size();
        }

        @Override
        protected void elementClicked(int index, boolean doubleClick) {

        }

        @Override
        protected boolean isSelected(int index) {
            return false;
        }

        @Override
        protected void drawBackground() {

        }

        @Override
        protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
            GlStateManager.pushMatrix();
            ItemStack stack = parent.tileConstructionTable.getInstructionRecipes().get(parent.tileConstructionTable.selectedRecipe).getRequirements().get(slotIdx);
            parent.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, this.left + 0, slotTop);

            parent.mc.renderEngine.bindTexture(CONSTRUCTION_TABLE_GUI_TEXTURES);
            if (parent.tileConstructionTable.hasPlayerThisItem(parent.mc.player.inventory, stack))
                parent.drawTexturedModalRect(this.left + 18, slotTop + 7, 176, 0, 6, 6);
            else
                parent.drawTexturedModalRect(this.left + 18, slotTop + 7, 183, 0, 7, 7);

            parent.mc.getRenderItem().renderItemOverlays(parent.fontRenderer, stack, this.left + 1, slotTop);
            GlStateManager.disableLighting();

            GlStateManager.popMatrix();
        }

    }

}
