package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityWebShoot;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.awt.*;

public class RenderWebShoot extends Render<EntityWebShoot> {

    public RenderWebShoot(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityWebShoot entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity != null && entity.getThrower() != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            Vec3d arrowPos = new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks, entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
            Vec3d playerPos = new Vec3d(entity.getThrower().prevPosX + (entity.getThrower().posX - entity.getThrower().prevPosX) * partialTicks, entity.getThrower().prevPosY + (entity.getThrower().posY - entity.getThrower().prevPosY) * partialTicks + entity.getThrower().height / 2F, entity.getThrower().prevPosZ + (entity.getThrower().posZ - entity.getThrower().prevPosZ) * partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();

            if (entity instanceof EntityWebShoot.EntityTaserWeb) {
                LCRenderHelper.setupRenderLightning();
                LCRenderHelper.drawGlowingLine(Vec3d.ZERO, playerPos.subtract(arrowPos), 0.1F, new Color(0.7F, 0.7F, 1F), 1F);
                LCRenderHelper.finishRenderLightning();
            } else if (entity instanceof EntityWebShoot.EntitySwingWeb || entity instanceof EntityWebShoot.EntityPullWeb) {
                LCRenderHelper.drawCuboidLine(Vec3d.ZERO, playerPos.subtract(arrowPos), 0.2F, new Color(1F, 1F, 1F), 1F);
            } else {
                RenderGlobal.renderFilledBox(new AxisAlignedBB(-entity.width / 2D, 0, -entity.width / 2D, entity.width / 2D, entity.height, entity.width / 2D), 1, 1, 1, 1);
            }

            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityWebShoot entity) {
        return null;
    }

}
