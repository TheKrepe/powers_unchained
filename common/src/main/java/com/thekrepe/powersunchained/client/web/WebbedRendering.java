package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.platform.Services;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Sticky web on entities: drawn over the entity's own model (any living entity), on the local player's arm in first
 * person and as a web overlay on the screen of a webbed player.
 */
public final class WebbedRendering {

    /** Tiling web pattern, drawn with the entity model's own UVs. */
    public static final Identifier ENTITY_TEXTURE = Constants.id("textures/entity/webbed.png");
    public static final Identifier SCREEN_TEXTURE = Constants.id("textures/misc/webbed_overlay.png");

    private WebbedRendering() {
    }

    public static boolean isWebbed(Entity entity) {
        return Services.GAMEPLAY.getWebbedUntil(entity) > entity.level().getGameTime();
    }

    /** Web drawn over the parent model; the loader tells whether a render state belongs to a webbed entity. */
    public abstract static class Layer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

        protected Layer(RenderLayerParent<S, M> parent) {
            super(parent);
        }

        protected abstract boolean isWebbed(S state);

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state, float yRot, float xRot) {
            if (this.isWebbed(state)) {
                collector.submitModel(this.getParentModel(), state, poseStack, RenderTypes.armorCutoutNoCull(ENTITY_TEXTURE),
                        lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            }
        }
    }

    /** First person: web over the local player's arm. */
    public static void renderArm(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, ModelPart arm, HumanoidArm side) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !isWebbed(mc.player)) {
            return;
        }
        arm.resetPose();
        arm.visible = true;
        arm.zRot = side == HumanoidArm.RIGHT ? 0.1F : -0.1F;
        collector.submitModelPart(arm, poseStack, RenderTypes.armorCutoutNoCull(ENTITY_TEXTURE), lightCoords, OverlayTexture.NO_OVERLAY, null);
    }

    /** HUD layer: webs in the corners of the screen while the local player is webbed. */
    public static void renderOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        long left = Services.GAMEPLAY.getWebbedUntil(mc.player) - mc.player.level().getGameTime();
        if (left <= 0) {
            return;
        }
        int alpha = (int) (Math.min(1.0F, left / 20.0F) * 0xC0);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCREEN_TEXTURE, 0, 0, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(),
                256, 256, 256, 256, (alpha << 24) | 0xFFFFFF);
    }
}
