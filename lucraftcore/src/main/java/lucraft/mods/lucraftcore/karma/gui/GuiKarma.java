package lucraft.mods.lucraftcore.karma.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.util.container.ContainerDummy;
import lucraft.mods.lucraftcore.util.gui.buttons.GuiButton10x;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.tabs.TabRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiKarma extends GuiContainer {

    public static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(LucraftCore.MODID, "textures/gui/karma.png");

    public GuiKarmaStatList list;
    public int selected;
    public int mouseX;
    public int mouseY;

    public GuiKarma() {
        super(new ContainerDummy());
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = 176;
        this.ySize = 166;

        int i = this.guiLeft;
        int j = this.guiTop;

        this.addButton(new GuiButton10x(0, i + this.xSize - 15 - 10, j + 145, "?"));

        TabRegistry.updateTabValues(i, j, InventoryTabKarma.class);
        TabRegistry.addTabsToList(this.buttonList);

        list = new GuiKarmaStatList(mc, this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.list != null) {
            this.list.handleMouseInput(this.mouseX, this.mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        String s = String.format(StringHelper.translateToLocal("lucraftcore.info.totalkarma"), new Object[]{KarmaHandler.getKarma(mc.player)});
        int l = fontRenderer.getStringWidth(s);
        fontRenderer.drawString(s, i + (this.ySize / 2) - (l / 2), j + 147, 4210752);

        if (list != null) {
            this.list.drawScreen(mouseX, mouseY, partialTicks);

            GuiButton info = this.buttonList.get(0);
            if (selected >= 0 && info.enabled && mouseX >= info.x && mouseX <= info.x + info.width && mouseY >= info.y && mouseY <= info.y + info.height) {
                KarmaStat stat = KarmaStat.getKarmaStats().get(selected);

                if (stat != null) {
                    List<String> list = new ArrayList<String>();
                    for (String s2 : mc.fontRenderer.listFormattedStringToWidth(StringHelper.translateToLocal(stat.getUnlocalizedDescription()), 150)) {
                        list.add(s2);
                    }
                    this.drawHoveringText(list, mouseX + 10, mouseY);
                }
            }
        }
    }

}
