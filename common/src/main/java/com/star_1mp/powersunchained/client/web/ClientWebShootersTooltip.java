package com.star_1mp.powersunchained.client.web;

import com.star_1mp.powersunchained.item.WebShootersItem;
import com.star_1mp.powersunchained.web.WebCartridges;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Web-shooters tooltip image: the cartridge slots in the bundle style, with the loaded cartridges and their fluid bars. */
public class ClientWebShootersTooltip implements ClientTooltipComponent {

    private static final Identifier SLOT = Identifier.withDefaultNamespace("container/bundle/slot_background");
    private static final int SLOT_SIZE = 24;

    private final List<Integer> cartridges;
    private final int slots;

    public ClientWebShootersTooltip(WebShootersItem.WebShootersTooltip tooltip) {
        this.cartridges = tooltip.cartridges();
        this.slots = tooltip.slots();
    }

    @Override
    public int getHeight(Font font) {
        return SLOT_SIZE + 4;
    }

    @Override
    public int getWidth(Font font) {
        return SLOT_SIZE * this.slots;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        for (int i = 0; i < this.slots; i++) {
            int sx = x + i * SLOT_SIZE;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT, sx, y, SLOT_SIZE, SLOT_SIZE);
            if (i < this.cartridges.size()) {
                ItemStack stack = WebCartridges.toStack(this.cartridges.get(i));
                graphics.item(stack, sx + 4, y + 4, i);
                graphics.itemDecorations(font, stack, sx + 4, y + 4);
            }
        }
    }
}
