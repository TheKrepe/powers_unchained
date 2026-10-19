package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityPortal;
import lucraft.mods.lucraftcore.util.helper.LCMathHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderPortal extends Render<EntityPortal> {

    public RenderPortal(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float radius = (entity.size / 2F) * ((float) entity.getOpening() / (float) EntityPortal.maxOpening);
        int corners = 10;
        Vec3d vec = new Vec3d(radius, 0, 0);
        Vec3d middle = new Vec3d(0, entity.height / 2F, 0);

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableLighting();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder bb = tes.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableFog();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableCull();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(0, entity.height / 2F, 0);
        GlStateManager.rotate(-entity.rotationYaw, 0, 1, 0);
        GlStateManager.rotate(entity.rotationPitch, 1, 0, 0);
        GlStateManager.translate(0, -entity.height / 2F, 0);

        for (int i = 0; i < corners; i++) {
            bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR);
            Vec3d pos1 = middle.add(LCMathHelper.rotateZ(vec, (float) Math.toRadians((360D / (double) corners) * (double) i)));
            Vec3d pos2 = middle.add(LCMathHelper.rotateZ(vec, (float) Math.toRadians((360D / (double) corners) * (double) (i + 1))));
            bb.pos(middle.x, middle.y, middle.z).color(0, 0, 0, 255).endVertex();
            bb.pos(pos1.x, pos1.y, pos1.z).color(0, 75, 108, 255).endVertex();
            bb.pos(pos2.x, pos2.y, pos2.z).color(0, 75, 108, 255).endVertex();
            tes.draw();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPortal entity) {
        return null;
    }

}
