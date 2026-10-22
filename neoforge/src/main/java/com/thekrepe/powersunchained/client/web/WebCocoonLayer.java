package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.entity.web.WebCocoon;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;

/**
 * Web cocoon on any living entity: its own model drawn once more on top with a web texture (like the charged creeper
 * glow), so the web wraps exactly around the mob's shape. Added to every living entity renderer.
 */
public class WebCocoonLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    public static final Identifier TEXTURE = Constants.id("textures/entity/web/web_cocoon.png");
    /** How much the cocoon covers the entity (0..1), set by {@link #extract}. */
    public static final ContextKey<Float> ALPHA = new ContextKey<>(Constants.id("web_cocoon"));

    public WebCocoonLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    /** Render state modifier for every living entity. */
    public static void extract(LivingEntity entity, LivingEntityRenderState state) {
        WebCocoon cocoon = WebCocoon.clientCocoon(entity);
        if (cocoon != null) {
            float partialTick = state.ageInTicks - Mth.floor(state.ageInTicks);
            state.setRenderData(ALPHA, WebCocoon.alpha(cocoon.tickCount + partialTick));
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
        Float alpha = state.getRenderData(ALPHA);
        if (alpha == null || alpha <= 0.0F) {
            return;
        }
        int color = Math.round(alpha * 255.0F) << 24 | 0xFFFFFF;
        collector.order(1).submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityTranslucent(TEXTURE), lightCoords,
                OverlayTexture.NO_OVERLAY, color, null, state.outlineColor);
    }
}
