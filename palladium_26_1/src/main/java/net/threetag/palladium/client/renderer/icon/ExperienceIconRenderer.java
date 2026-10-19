package net.threetag.palladium.client.renderer.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.icon.ExperienceIcon;
import net.threetag.palladium.icon.SpriteIcon;
import net.threetag.palladium.logic.context.DataContext;

public class ExperienceIconRenderer implements IconRenderer<ExperienceIcon> {

    private static final SpriteIcon BACKGROUND_ICON = new SpriteIcon(Palladium.id("icon/experience"));

    @Override
    public void draw(ExperienceIcon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha) {
        IconRenderer.drawIcon(BACKGROUND_ICON, mc, guiGraphics, context, x, y, width, height, alpha);

        var stack = guiGraphics.pose();
        stack.pushMatrix();
        stack.translate(x, y);

        if (width != 16 || height != 16) {
            stack.scale(width / 16F, height / 16F);
        }

        String text = icon.amount() + (icon.level() ? "L" : "");
        guiGraphics.text(mc.font, text, 9, 8, 0, false);
        guiGraphics.text(mc.font, text, 7, 8, 0, false);
        guiGraphics.text(mc.font, text, 8, 9, 0, false);
        guiGraphics.text(mc.font, text, 8, 7, 0, false);
        guiGraphics.text(mc.font, text, 8, 8, 8453920, false);

        stack.popMatrix();
    }
}
