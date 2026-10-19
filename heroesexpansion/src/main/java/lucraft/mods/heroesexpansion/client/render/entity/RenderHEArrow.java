package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityHEArrow;
import lucraft.mods.heroesexpansion.items.ItemHEArrow.ArrowType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderHEArrow extends RenderArrow<EntityHEArrow> {

    public ArrowType type;

    public RenderHEArrow(RenderManager renderManagerIn, ArrowType type) {
        super(renderManagerIn);
        this.type = type;
    }

    @Override
    public void doRender(EntityHEArrow entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) y, (float) z);

        if (entity.getClass() == ArrowType.GRAPPLING_HOOK.getEntityClass() && entity != null && entity.shootingEntity != null) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.glLineWidth(3F);
            GlStateManager.color(0.25F, 0.125F, 0, 1);
            BufferBuilder bb = Tessellator.getInstance().getBuffer();
            bb.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
            bb.pos(0, 0, 0).endVertex();
            Vec3d arrowPos = new Vec3d(entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks, entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks, entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
            Vec3d playerPos = new Vec3d(entity.shootingEntity.prevPosX + (entity.shootingEntity.posX - entity.shootingEntity.prevPosX) * partialTicks, entity.shootingEntity.prevPosY + (entity.shootingEntity.posY - entity.shootingEntity.prevPosY) * partialTicks, entity.shootingEntity.prevPosZ + (entity.shootingEntity.posZ - entity.shootingEntity.prevPosZ) * partialTicks);
            bb.pos(playerPos.x - arrowPos.x, playerPos.y + (entity.shootingEntity.height / 2D) - arrowPos.y, playerPos.z - arrowPos.z).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }

        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        this.type.renderArrow(entity, false, false, entity.color, Minecraft.getMinecraft());

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

    }

    @Override
    protected ResourceLocation getEntityTexture(EntityHEArrow entity) {
        return null;
    }

}
