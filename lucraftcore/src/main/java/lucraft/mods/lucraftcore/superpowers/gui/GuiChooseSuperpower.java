package lucraft.mods.lucraftcore.superpowers.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.network.MessageChooseSuperpower;
import lucraft.mods.lucraftcore.util.container.ContainerDummy;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChooseSuperpower extends GuiContainer {

    public static final ResourceLocation TEX = new ResourceLocation(LucraftCore.MODID, "textures/gui/choose_superpower.png");

    public List<Superpower> superpowers = new ArrayList<>();
    public GuiChooseSuperpowerList list;
    public int selected = -1;

    public GuiChooseSuperpower(String[] superpowers) {
        super(new ContainerDummy());

        for (String s : superpowers) {
            Superpower sp = SuperpowerHandler.SUPERPOWER_REGISTRY.getValue(new ResourceLocation(s));
            if (sp != null)
                this.superpowers.add(sp);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.xSize = 256;
        this.ySize = 189;
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        list = new GuiChooseSuperpowerList(mc, this);

        this.addButton(new GuiButtonExt(0, i + 5, j + 167, 70, 18, StringHelper.translateToLocal("lucraftcore.info.confirm")));
        this.addButton(new GuiButtonExt(1, i + 180, j + 167, 70, 18, StringHelper.translateToLocal("lucraftcore.info.close")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            LCPacketDispatcher.sendToServer(new MessageChooseSuperpower(superpowers.get(selected).getRegistryName().toString()));
            mc.player.closeScreen();
        } else if (button.id == 1)
            mc.player.closeScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        mc.getTextureManager().bindTexture(TEX);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        String s = StringHelper.translateToLocal("lucraftcore.info.chooseasuperpower");
        int width = fontRenderer.getStringWidth(s);
        fontRenderer.drawString(s, i + this.xSize / 2 - width / 2, j + 6, 4210752);

        if (list != null) {
            list.drawScreen(mouseX, mouseY, partialTicks);
        }

        this.buttonList.get(0).enabled = selected >= 0;
    }

}
