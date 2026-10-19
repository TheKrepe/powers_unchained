package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.client.render.tileentity.TESRPortalDevice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;

public class ItemRendererPortalDevice extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TESRPortalDevice.TEXTURE_PORTAL_DEVICE);
        GlStateManager.translate(0.5F, 1F, 0.5F);
        GlStateManager.rotate(180, 1, 0, 0);
        GlStateManager.rotate(90, 0, 1, 0);
        TESRPortalDevice.MODEL_PORTAL_DEVICE.renderModel(0.0625F);
    }
}
