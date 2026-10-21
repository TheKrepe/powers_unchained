package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.entity.web.AbstractWebProjectile;
import com.thekrepe.powersunchained.entity.web.StickyWeb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Draws web shots as a four-leaved web star: small and pointed while flying, spreading out flat on the surface it
 * hits; sticky webs follow the entity they stick to. From TheKrepe's Fisk's Superheroes port (used with permission),
 * with our own textures.
 */
public class WebProjectileRenderer<T extends AbstractWebProjectile> extends EntityRenderer<T, WebProjectileRenderer.State> {

    public static final Identifier TEXTURE = Constants.id("textures/entity/web/web.png");
    public static final Identifier TEXTURE_LARGE = Constants.id("textures/entity/web/web_large.png");
    private static final int[] ROT_MATRIX = {0, 0, 2, 0, 3, 1};

    private final float webSize;
    private final Identifier texture;

    public WebProjectileRenderer(EntityRendererProvider.Context context, float size, Identifier texture) {
        super(context);
        this.webSize = size;
        this.texture = texture;
    }

    public static <T extends AbstractWebProjectile> WebProjectileRenderer<T> impact(EntityRendererProvider.Context context) {
        return new WebProjectileRenderer<>(context, 8.0F, TEXTURE);
    }

    public static <T extends AbstractWebProjectile> WebProjectileRenderer<T> sticky(EntityRendererProvider.Context context) {
        return new WebProjectileRenderer<>(context, 12.0F, TEXTURE_LARGE);
    }

    @Override
    protected boolean affectedByCulling(T entity) {
        return !(entity instanceof StickyWeb web && web.getHitEntity() != null);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        float grapple = Mth.lerp(partialTick, entity.prevGrappleTimer, entity.grappleTimer);
        state.in = Mth.lerp(grapple, 8.0F, -1.5F) / 16.0F;
        state.w = Mth.lerp(grapple, 2.0F, this.webSize) / 32.0F;
        state.alpha = Mth.clamp(1.0F - (entity.tickCount + partialTick - 20.0F) / (entity.getLifespan() - 20), 0.0F, 1.0F);
        state.inGround = entity.inGround;
        state.dir = Direction.from3DDataValue(entity.blockSide);
        state.yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        state.xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.spin = entity.getId() * 20;
        state.halfHeight = entity.getBbHeight() / 2.0F;
        state.onEntity = false;

        LivingEntity target = entity instanceof StickyWeb web ? web.getHitEntity() : null;
        if (target != null) {
            StickyWeb web = (StickyWeb) entity;
            Vec3 feet = target.getPosition(partialTick);
            state.offset = feet.subtract(state.x, state.y, state.z);
            state.onEntity = true;
            Minecraft mc = Minecraft.getInstance();
            boolean firstPerson = target == mc.player && mc.options.getCameraType().isFirstPerson();
            Vec3 hit = web.getHitOffset();
            double height = hit.y;
            boolean head = !firstPerson && StickyWeb.isOnHead(target, height);
            state.bodyYaw = head ? Mth.rotLerp(partialTick, target.yHeadRotO, target.yHeadRot)
                    : Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot);

            // the web lies on the body's outline at the spot it hit (an ellipse: for humanoids the head is a box,
            // the torso + arms are wide and flat, the legs narrower), turned to face outwards from there
            float side;
            float front;
            if (StickyWeb.isHumanoid(target)) {
                float s = target.getBbWidth() / 0.6F;
                float h = target.getBbHeight();
                if (head) {
                    side = front = 0.29F * s;
                } else if (height > h * 0.4F) {
                    side = 0.4F * s;
                    front = 0.17F * s;
                } else {
                    side = 0.27F * s;
                    front = 0.17F * s;
                }
            } else {
                side = front = target.getBbWidth() / 2.0F * 0.95F;
            }
            if (firstPerson) {
                // webs on yourself in first person stay below the camera (chest / legs): seen when looking down
                // instead of covering the screen
                height = Math.min(height, target.getEyeHeight() - 0.95);
            }
            state.local = new Vec3(hit.x * side, height, hit.z * front);
            state.faceYaw = (float) Math.toDegrees(Math.atan2(hit.x / side, hit.z / front));
            float scale = Mth.clamp(target.getBbWidth() / 0.6F, 0.5F, 1.5F);
            state.w *= firstPerson ? scale * 0.6F : head ? scale * 0.75F : scale;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.onEntity) {
            // target's feet -> body space (forward = +Z) -> spot on the body -> facing outwards
            poseStack.translate(state.offset.x, state.offset.y, state.offset.z);
            poseStack.rotateDegrees(Axis.YP, -state.bodyYaw);
            poseStack.translate(state.local.x, state.local.y, state.local.z);
            poseStack.rotateDegrees(Axis.YP, state.faceYaw);
            poseStack.translate(0.0F, 0.0F, 0.065F);
        } else {
            poseStack.translate(0.0F, state.halfHeight, 0.0F);
            if (state.inGround) {
                if (state.dir.getStepY() != 0) {
                    poseStack.rotateDegrees(Axis.XP, -90 * state.dir.getStepY());
                } else {
                    poseStack.rotateDegrees(Axis.YP, 90 * ROT_MATRIX[state.dir.ordinal()]);
                }
            } else {
                poseStack.rotateDegrees(Axis.YP, state.yRot + 180.0F);
                poseStack.rotateDegrees(Axis.XP, state.xRot);
            }
        }
        poseStack.rotateDegrees(Axis.ZP, state.spin);
        poseStack.translate(0.0F, 0.0F, -0.05875F);

        int color = WebGeometry.color(state.alpha);
        int light = state.lightCoords;
        float w = state.w;
        float in = state.in;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(this.texture),
                (pose, consumer) -> WebGeometry.webShape(pose, consumer, w, in, color, light));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class State extends EntityRenderState {
        public float in;
        public float w;
        public float alpha = 1.0F;
        public boolean inGround;
        public Direction dir = Direction.DOWN;
        public float yRot;
        public float xRot;
        public float spin;
        public float halfHeight;
        public boolean onEntity;
        public Vec3 offset = Vec3.ZERO;
        public float bodyYaw;
        /** Spot on the body in body space (forward = +Z), from the feet. */
        public Vec3 local = Vec3.ZERO;
        public float faceYaw;
    }
}
