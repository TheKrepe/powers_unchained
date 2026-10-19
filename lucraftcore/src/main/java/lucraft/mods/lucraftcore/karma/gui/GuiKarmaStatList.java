package lucraft.mods.lucraftcore.karma.gui;

import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.lwjgl.opengl.GL11;

public class GuiKarmaStatList extends GuiScrollingList {

    private GuiKarma parent;

    public GuiKarmaStatList(Minecraft client, GuiKarma parent) {
        super(client, 146, 125, ((parent.height - parent.getYSize()) / 2) + 15, ((parent.height - parent.getYSize()) / 2) + 15 + 125, ((parent.width - parent.getXSize()) / 2) + 15, 27, parent.width, parent.height);
        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return KarmaStat.getKarmaStats().size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        parent.mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 1, 1);
        parent.selected = index;
    }

    @Override
    protected boolean isSelected(int index) {
        return index == parent.selected;
    }

    @Override
    protected void drawBackground() {

    }

    @Override
    protected int getContentHeight() {
        return super.getContentHeight();
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        KarmaStat stat = KarmaStat.getKarmaStats().get(slotIdx);
        int statAmount = KarmaHandler.getKarmaStat(parent.mc.player, stat);
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1);
        parent.mc.fontRenderer.drawString(TextFormatting.UNDERLINE + StringHelper.translateToLocal(stat.getUnlocalizedName()) + ": " + statAmount, this.left + 5, slotTop + 2, 0xfefefe);
        parent.mc.fontRenderer.drawString("" + TextFormatting.GRAY + TextFormatting.ITALIC + " (* " + stat.getAmount() + " = " + (stat.getAmount() * statAmount) + ")", this.left + 5, slotTop + 13, 0x939393);
        GlStateManager.disableBlend();
    }

}