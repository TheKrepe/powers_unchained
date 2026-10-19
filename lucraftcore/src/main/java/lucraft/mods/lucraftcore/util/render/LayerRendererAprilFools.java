package lucraft.mods.lucraftcore.util.render;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class LayerRendererAprilFools implements LayerRenderer<EntityPlayer> {

    private static boolean downloaded = false;
    private static ResourceLocation TEXTURE = new ResourceLocation(LucraftCore.NAME, "april_fools_overlay");
    public RenderLivingBase<?> renderer;

    public LayerRendererAprilFools(RenderLivingBase<?> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!downloaded)
            return;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.blendFunc(770, 771);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        this.renderer.getMainModel().render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.blendFunc(771, 770);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    public static boolean isAprilFoolsDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static void download() {
        new Thread(() -> {
            try {
                BufferedImage image = ImageIO.read(new URL("https://i.imgur.com/Ya82J5k.png"));
                Minecraft.getMinecraft().getTextureManager().loadTexture(TEXTURE, new DynamicTexture(image));
                downloaded = true;
            } catch (IOException e) {
                e.printStackTrace();
                downloaded = false;
            }
        }).run();
    }

}
