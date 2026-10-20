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
import org.jspecify.annotations.Nullable;

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
            Vec3 pos = target.getPosition(partialTick);
            state.offset = pos.subtract(state.x, state.y, state.z);
            state.onEntity = true;
            Minecraft mc = Minecraft.getInstance();
            boolean firstPerson = target == mc.player && mc.options.getCameraType().isFirstPerson();
            state.targetYaw = firstPerson ? Mth.lerp(partialTick, target.yRotO, target.getYRot())
                    : Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot) - 180.0F;
            state.hitOffset = web.getHitOffset();
            if (firstPerson) {
                // webs on yourself in first person: keep them below the camera (on the chest / legs), so they are
                // seen when looking down instead of covering the screen
                Vec3 o = state.hitOffset;
                state.hitOffset = new Vec3(o.x, Math.min(o.y, target.getEyeHeight() - 0.95), o.z);
                state.w *= 0.6F;
            }
            state.side = target.getBbWidth() / 1.5F;
            state.front = target.getBbWidth() / 1.75F;
            state.webYaw = web.getYawOffset() + entity.getYRot() + 180.0F;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.onEntity) {
            poseStack.translate(state.offset.x, state.offset.y + state.halfHeight, state.offset.z);
            poseStack.rotateDegrees(Axis.YP, -state.targetYaw);
            if (state.hitOffset != null) {
                poseStack.translate(state.hitOffset.x * state.side, state.hitOffset.y, state.hitOffset.z * state.front);
            }
            poseStack.rotateDegrees(Axis.YP, state.webYaw);
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
        boolean flat = !state.inGround || state.onEntity;
        Direction dir = state.dir;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(this.texture), (pose, consumer) -> {
            if (flat) {
                WebGeometry.webShape(pose, consumer, w, in, color, light, 0.0F, 1.0F, 0.0F);
            } else {
                WebGeometry.webShape(pose, consumer, w, in, color, light, dir.getStepX(), dir.getStepY(), dir.getStepZ());
            }
        });
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
        public float targetYaw;
        public @Nullable Vec3 hitOffset;
        public float side;
        public float front;
        public float webYaw;
    }
}
