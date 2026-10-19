package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntityCaptainAmericaShield;
import lucraft.mods.heroesexpansion.items.ItemCaptainAmericaShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderCaptainAmericaShield extends Render<EntityCaptainAmericaShield> {

    public RenderCaptainAmericaShield(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityCaptainAmericaShield entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x, y, z);
        double speed = MathHelper.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);

        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0, 1, 0);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);
        GlStateManager.rotate((float) ((entity.ticksExisted + partialTicks) * speed * 20F), 0, 1, 0);

        if (!(entity.getItem().getItem() instanceof ItemCaptainAmericaShield)) {
            GlStateManager.rotate(270, 1, 0, 0);
            float scale = 4F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(-0.1D, -0.1D, 0);
        }

        Minecraft.getMinecraft().getRenderItem().renderItem(entity.getItem(), ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCaptainAmericaShield entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

}
