package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityBillyClubHook;
import lucraft.mods.heroesexpansion.entities.EntityThrownBillyClub;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;

public class RenderBillyClub extends Render<EntityThrownBillyClub> {

    public RenderBillyClub(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrownBillyClub entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x, y, z);

        if (entity instanceof EntityBillyClubHook && entity.getThrower() != null) {
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            LCRenderHelper.drawCuboidLine(Vec3d.ZERO, entity.getThrower().getPositionVector().subtract(entity.getPositionVector()), 0.2F, new Color(0.3F, 0.3F, 0.3F), 1F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0, 1, 0);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);
        GlStateManager.rotate(90F, 0, 1, 0);
        Minecraft.getMinecraft().getRenderItem().renderItem(entity.getItem(), ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityThrownBillyClub entity) {
        return null;
    }
}
