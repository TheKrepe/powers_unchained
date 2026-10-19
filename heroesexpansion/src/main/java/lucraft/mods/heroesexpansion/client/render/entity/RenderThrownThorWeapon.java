package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.client.render.item.ItemRendererMjolnir;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererStormbreaker;
import lucraft.mods.heroesexpansion.entities.EntityThrownThorWeapon;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderThrownThorWeapon extends Render<EntityThrownThorWeapon> {

    public RenderThrownThorWeapon(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityThrownThorWeapon entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x, y, z);

        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0, 1, 0);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);

        this.bindEntityTexture(entity);

        if (entity.item.getItem() instanceof ItemThorWeapon)
            ((ItemThorWeapon) entity.item.getItem()).renderThrownEntityModel(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityThrownThorWeapon entity) {
        if (entity.item.getItem() == HEItems.MJOLNIR)
            return ItemRendererMjolnir.MJOLNIR_TEX;
        else if (entity.item.getItem() == HEItems.STORMBREAKER)
            return ItemRendererStormbreaker.STORMBREAKER_TEX;
        else
            return null;
    }

}
