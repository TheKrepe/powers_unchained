package net.threetag.palladium.client.renderer.icon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.threetag.palladium.icon.ItemInSlotIcon;
import net.threetag.palladium.logic.context.DataContext;

public class ItemInSlotIconRenderer implements IconRenderer<ItemInSlotIcon> {

    @Override
    public void draw(ItemInSlotIcon icon, Minecraft mc, GuiGraphicsExtractor guiGraphics, DataContext context, int x, int y, int width, int height, float alpha) {
        var stack = guiGraphics.pose();
        stack.pushMatrix();
        stack.translate(x + width / 2F, y + height / 2F);

        if (width != 16 || height != 16) {
            int s = Math.min(width, height);
            stack.scale(s / 16F, s / 16F);
        }

        var item = new ItemStack(Items.BARRIER);
        var items = icon.slot().getItems(context.getLivingEntity());

        if (!items.isEmpty()) {
            var found = items.getFirst();

            if (!found.isEmpty()) {
                item = found;
            }
        }

        guiGraphics.item(item, -8, -8);
        stack.popMatrix();
    }

}
