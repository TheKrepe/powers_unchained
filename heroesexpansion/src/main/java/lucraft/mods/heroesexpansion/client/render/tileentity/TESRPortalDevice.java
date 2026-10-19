package lucraft.mods.heroesexpansion.client.render.tileentity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelPortalDevice;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class TESRPortalDevice extends TileEntitySpecialRenderer<TileEntityPortalDevice> {

    public static final ModelPortalDevice MODEL_PORTAL_DEVICE = new ModelPortalDevice();
    public static final ResourceLocation TEXTURE_PORTAL_DEVICE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/portal_device.png");

    @Override
    public void render(TileEntityPortalDevice te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        int i = !te.hasWorld() ? 0 : te.getBlockMetadata();

        if (i == 2)
            GlStateManager.rotate(-90, 0, 1, 0);
        else if (i == 3)
            GlStateManager.rotate(90, 0, 1, 0);
        else if (i == 5)
            GlStateManager.rotate(180, 0, 1, 0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.5F, 0);
        GlStateManager.rotate(180, 1, 0, 0);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_PORTAL_DEVICE);
        MODEL_PORTAL_DEVICE.renderModel(0.0625F);
        GlStateManager.popMatrix();

        if (te.progress > 0 && te.prevProgress > 0) {
            double length = 20;
            float progress = (te.prevProgress + (te.progress - te.prevProgress) * partialTicks) / (float) TileEntityPortalDevice.maxProgress;
            LCRenderHelper.setupRenderLightning();
            LCRenderHelper.drawGlowingLine(new Vec3d(0, 0.8F, 0), new Vec3d(0, 0.8F + (length - 0.3F) * progress, 0), 1, new Color(0.28F, 1F, 1F));
            LCRenderHelper.finishRenderLightning();
        }

        if (!te.getStackInSlot(1).isEmpty()) {
            GlStateManager.translate(0, 0.85F, 0);
            GlStateManager.scale(0.25F, 0.25F, 0.25F);
            GlStateManager.rotate(Minecraft.getMinecraft().player.ticksExisted + partialTicks, 0, 1, 0);
            GlStateManager.rotate(MathHelper.sin((Minecraft.getMinecraft().player.ticksExisted + partialTicks) / 10F) * 10F, 1, 0, 0);
            Minecraft.getMinecraft().getRenderItem().renderItem(te.getStackInSlot(1), ItemCameraTransforms.TransformType.FIXED);
        }

        GlStateManager.popMatrix();
    }
}
