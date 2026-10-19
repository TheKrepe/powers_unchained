package lucraft.mods.lucraftcore.util.gui.buttons;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButton10x extends GuiButton {

    protected static final ResourceLocation buttonTextures = new ResourceLocation(LucraftCore.MODID, "textures/gui/button_10x.png");

    public boolean unicode = false;

    public GuiButton10x(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, 10, 10, buttonText);
    }

    public GuiButton10x(int buttonId, int x, int y, int width, String buttonText) {
        super(buttonId, x, y, width, 10, buttonText);
    }

    public GuiButton10x(int buttonId, int x, int y, int width, String buttonText, boolean unicode) {
        super(buttonId, x, y, width, 10, buttonText);
        this.unicode = unicode;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, i * 10, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, i * 10, this.width / 2, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0) {
                j = packedFGColour;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            boolean b = mc.fontRenderer.getUnicodeFlag();
            if (unicode)
                mc.fontRenderer.setUnicodeFlag(true);

            String buttonText = this.displayString;
            int strWidth = mc.fontRenderer.getStringWidth(buttonText);
            int ellipsisWidth = mc.fontRenderer.getStringWidth("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                buttonText = mc.fontRenderer.trimStringToWidth(buttonText, width - 6 - ellipsisWidth).trim() + "...";

            this.drawCenteredString(fontrenderer, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);

            if (unicode)
                mc.fontRenderer.setUnicodeFlag(b);
        }
    }

}