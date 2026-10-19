package net.threetag.palladium.client.renderer.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.threetag.palladium.icon.IngredientIcon;
import net.threetag.palladium.logic.context.DataContext;

public class IngredientIconRenderer implements IconRenderer<IngredientIcon> {

    @SuppressWarnings("deprecation")
    @Override
    public void draw(IngredientIcon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha) {
        var stack = guiGraphics.pose();
        stack.pushMatrix();
        stack.translate(x + width / 2F, y + height / 2F);

        if (width != 16 || height != 16) {
            int s = Math.min(width, height);
            stack.scale(s / 16F, s / 16F);
        }

        var items = icon.ingredient().items().toList();
        int stackIndex = (int) ((System.currentTimeMillis() / 1000) % items.size());
        guiGraphics.item(items.get(stackIndex).value().getDefaultInstance(), -8, -8);
        stack.popMatrix();
    }
}
