package lucraft.mods.heroesexpansion.client.render.entity;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityRadioactiveSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.util.ResourceLocation;

public class RenderRadioactiveSpider extends RenderSpider<EntityRadioactiveSpider> {

    private static final ResourceLocation RADIOACTIVE_SPIDER_TEXTURES = new ResourceLocation(HeroesExpansion.MODID, "textures/models/radioactive_spider.png");

    public RenderRadioactiveSpider(RenderManager renderManagerIn) {
        super(renderManagerIn);
        this.shadowSize *= 0.175F;
    }

    @Override
    protected void preRenderCallback(EntityRadioactiveSpider entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.175F, 0.175F, 0.175F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRadioactiveSpider entity) {
        return RADIOACTIVE_SPIDER_TEXTURES;
    }

}
