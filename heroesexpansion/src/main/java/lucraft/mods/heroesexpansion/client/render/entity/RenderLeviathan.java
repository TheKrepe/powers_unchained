package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelLeviathan;
import lucraft.mods.heroesexpansion.entities.EntityLeviathan;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderLeviathan extends RenderLiving<EntityLeviathan> {

    public static ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/leviathan.png");

    public RenderLeviathan(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelLeviathan(), 2F);
    }

    @Override
    protected void preRenderCallback(EntityLeviathan entitylivingbaseIn, float partialTickTime) {
        float f = 6F;
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(0, 1, 0);
        float pitch = (float) (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * 4F;
        GlStateManager.rotate(pitch, 1, 0, 0);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityLeviathan entity) {
        return TEXTURE;
    }
}
