package lucraft.mods.lucraftcore.infinity.render;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;

public class RenderEntityInfinityStone<T extends EntityItemIndestructible> extends Render<EntityItemIndestructible> {

    public RenderEntityInfinityStone(RenderManager rm) {
        super(rm);
    }

    @Override
    public void doRender(EntityItemIndestructible entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.getItem().isEmpty())
            return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + 0.17F, z);
        GlStateManager.rotate(-entity.rotationYaw, 0, 1, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(entity.getItem(), ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityItemIndestructible entity) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static void renderStone(Color color, Color glintColor, float size) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        float f = (float) Minecraft.getMinecraft().player.ticksExisted + LCRenderHelper.renderTick;
        LCRenderHelper.setLightmapTextureCoords(240, 240);
        GlStateManager.scale(size, size, size);
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1);

        ModelCube model = new ModelCube();

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(LucraftCore.MODID, "textures/models/stone.png"));
        GlStateManager.enableBlend();
        model.renderModel(0.0625F);

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        GlStateManager.color(glintColor.getRed() / 255F, glintColor.getGreen() / 255F, glintColor.getBlue() / 255F, 0.5F);

        for (int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            model.renderModel(0.0625F);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();

        GlStateManager.enableLighting();
        LCRenderHelper.restoreLightmapTextureCoords();
        GlStateManager.popMatrix();
    }
}
