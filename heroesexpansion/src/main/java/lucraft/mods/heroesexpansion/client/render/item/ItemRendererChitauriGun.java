package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelChitauriGun;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemRendererChitauriGun extends TileEntityItemStackRenderer {

    public static final ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/chitauri_gun.png");
    public static final ResourceLocation TEXTURE_GLOW = new ResourceLocation(HeroesExpansion.MODID, "textures/models/chitauri_gun_glow.png");
    public static final ModelChitauriGun MODEL = new ModelChitauriGun();

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180, 1, 0, 0);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        MODEL.renderModel(0.0625F);
        GlStateManager.disableLighting();
        LCRenderHelper.setLightmapTextureCoords(240, 240);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_GLOW);
        MODEL.renderModel(0.0625F);
        LCRenderHelper.restoreLightmapTextureCoords();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
