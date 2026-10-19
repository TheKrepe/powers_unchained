package lucraft.mods.lucraftcore.infinity.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ItemRendererInfinityStone extends TileEntityItemStackRenderer {

    public Color color;
    public Color glintColor;

    public ItemRendererInfinityStone(Color color, Color glintColor) {
        this.color = color;
        this.glintColor = glintColor;
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.translate(0.5F, -0.1F, 0.5F);
        RenderEntityInfinityStone.renderStone(color, glintColor, 0.4F);
    }
}
