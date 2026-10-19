package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.entities.EntitySound;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderSound extends Render<EntitySound> {

    public RenderSound(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntitySound entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        boolean flag = entity.isSneaking();
        float f = this.renderManager.playerViewY;
        float f1 = this.renderManager.playerViewX;
        boolean flag1 = this.renderManager.options.thirdPersonView == 2;
        float f2 = entity.height + 0.5F - (flag ? 0.25F : 0.0F);
        int i = "deadmau5".equals(entity.getDisplayName().getFormattedText()) ? -10 : 0;
        EntityRenderer.drawNameplate(this.getFontRendererFromRenderManager(), entity.getDisplayName().getFormattedText(), (float) x, (float) y + f2, (float) z, i, f, f1, flag1, flag);
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntitySound entity) {
        return null;
    }
}
