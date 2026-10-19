package net.threetag.palladium.client.renderer.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.threetag.palladium.icon.SpriteIcon;
import net.threetag.palladium.logic.context.DataContext;

public class SpriteIconRenderer implements IconRenderer<SpriteIcon> {

    @Override
    public void draw(SpriteIcon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.sprite(), x, y, width, height, alpha);
    }
}
