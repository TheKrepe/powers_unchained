package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelTesseract;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Random;

public class ItemRendererTesseract extends TileEntityItemStackRenderer {

    public static final ModelTesseract MODEL = new ModelTesseract();
    public static final ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/tesseract.png");

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Random rand = new Random(1);
        float f = 0.2F;
        Color color = new Color(0.4F, 0.4F, 1F);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, -0.8F, 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        LCRenderHelper.setLightmapTextureCoords(240, 240);

        LCRenderHelper.setupRenderLightning();
        GlStateManager.translate(0, 1.3F, 0);
        GlStateManager.rotate((mc.player.ticksExisted + LCRenderHelper.renderTick) / 2F, 0, 1, 0);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.9F, 0.9F, 0.9F);
        for (int i = 0; i < 30; i++) {
            GlStateManager.rotate((mc.player.ticksExisted + LCRenderHelper.renderTick) * i / 70F, 1, 1, 0);
            LCRenderHelper.drawGlowingLine(new Vec3d((-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f), new Vec3d((-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f), 0.1F, color, 0);
        }
        GlStateManager.popMatrix();

        LCRenderHelper.drawGlowingLine(new Vec3d(-0.05F, 0, 0), new Vec3d(0.05F, 0, 0), 0.9F, color);

        LCRenderHelper.finishRenderLightning();

        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(TEXTURE);
        MODEL.renderModel(0.0625F);

        LCRenderHelper.restoreLightmapTextureCoords();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

    }

}
