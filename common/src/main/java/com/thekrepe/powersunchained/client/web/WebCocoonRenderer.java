package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.entity.web.WebCocoon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** The web cocoon: a web-covered box around the wrapped entity, fading in and out. */
public class WebCocoonRenderer extends EntityRenderer<WebCocoon, WebCocoonRenderer.State> {

    public static final Identifier TEXTURE = Constants.id("textures/entity/web/web_cocoon.png");

    public WebCocoonRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected boolean affectedByCulling(WebCocoon entity) {
        return false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WebCocoon entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        LivingEntity target = entity.getTarget();
        state.visible = target != null;
        if (target == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // your own cocoon in first person shows as the web overlay on the screen
        state.visible = !(target == mc.player && mc.options.getCameraType().isFirstPerson());
        state.offset = target.getPosition(partialTick).subtract(state.x, state.y, state.z);
        state.yaw = Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot);
        state.halfWidth = target.getBbWidth() / 2.0F + 0.08F;
        state.height = target.getBbHeight() + 0.06F;
        state.alpha = alpha(entity.tickCount + partialTick);
    }

    /** Fades in over 3 ticks and out over the last 10. */
    public static float alpha(float age) {
        return Mth.clamp(Math.min(age / 3.0F, (WebCocoon.DURATION - age) / 10.0F), 0.0F, 1.0F);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.visible && state.alpha > 0.0F) {
            poseStack.pushPose();
            Vec3 o = state.offset;
            poseStack.translate(o.x, o.y, o.z);
            poseStack.rotateDegrees(Axis.YP, -state.yaw);
            float hw = state.halfWidth;
            float h = state.height;
            int color = WebGeometry.color(state.alpha * 0.95F);
            int light = state.lightCoords;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TEXTURE),
                    (pose, consumer) -> WebGeometry.tiledBox(pose, consumer, -hw, -0.03F, -hw, hw, h - 0.03F, hw, 0.5F, color, light));
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public boolean visible;
        public Vec3 offset = Vec3.ZERO;
        public float yaw;
        public float halfWidth;
        public float height;
        public float alpha;
    }
}
