package com.star_1mp.powersunchained.client.irradiation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.entity.spider.RadioactiveSpider;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Radioactive Spider: the vanilla cave spider model and eyes (owner), our dark-blue texture, and the red patterns
 * glowing in a slow pulse (an emissive layer with only the pattern pixels, brightness = the pulse).
 */
public class RadioactiveSpiderRenderer extends SpiderRenderer<RadioactiveSpider> {

    private static final Identifier TEXTURE = Constants.id("textures/entity/radioactive_spider/radioactive_spider.png");
    private static final RenderType GLOW = RenderTypes.eyes(Constants.id("textures/entity/radioactive_spider/radioactive_spider_glow.png"));

    public RadioactiveSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.CAVE_SPIDER);
        this.shadowRadius = 0.56F;
        this.addLayer(new GlowLayer(this));
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    private static final class GlowLayer extends RenderLayer<LivingEntityRenderState, SpiderModel> {

        GlowLayer(RenderLayerParent<LivingEntityRenderState, SpiderModel> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, LivingEntityRenderState state, float yRot, float xRot) {
            if (state.deathTime > 0.0F) {
                return;  // "goes out" when it dies
            }
            // a slow, eased breathing glow (never fully off, so the shaded red stays readable)
            float t = 0.5F + 0.5F * Mth.sin(state.ageInTicks * 0.07F);
            float pulse = 0.4F + 0.6F * (t * t * (3.0F - 2.0F * t));
            int color = ARGB.colorFromFloat(1.0F, pulse, pulse, pulse);
            collector.order(1).submitModel(this.getParentModel(), state, poseStack, GLOW, light, OverlayTexture.NO_OVERLAY, color, null,
                    state.outlineColor);
        }
    }
}
