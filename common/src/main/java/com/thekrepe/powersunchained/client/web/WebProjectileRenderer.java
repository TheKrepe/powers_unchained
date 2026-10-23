package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.entity.web.AbstractWebProjectile;
import com.thekrepe.powersunchained.entity.web.StickyWeb;
import com.thekrepe.powersunchained.entity.web.WebSwingLine;
import com.thekrepe.powersunchained.entity.web.WebZipLine;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Draws web shots as a four-leaved web star: small and pointed while flying, spreading out flat on the surface it
 * hits; sticky webs follow the entity they stick to (on the body's outline, on the faces of the head), web zip lines
 * also draw the line back to the shooter's wrist. From TheKrepe's Fisk's Superheroes port (used with permission),
 * with our own textures.
 */
public class WebProjectileRenderer<T extends AbstractWebProjectile> extends EntityRenderer<T, WebProjectileRenderer.State> {

    public static final Identifier TEXTURE = Constants.id("textures/entity/web/web.png");
    public static final Identifier TEXTURE_LARGE = Constants.id("textures/entity/web/web_large.png");
    public static final Identifier TEXTURE_ROPE = Constants.id("textures/entity/web/web_rope.png");
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
        return !(entity instanceof StickyWeb web && web.getHitEntity() != null) && !(entity instanceof WebZipLine);
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
        state.drawLine = false;
        state.headPitch = 0.0F;
        state.headPivot = 0.0F;

        if (entity instanceof WebZipLine line) {
            this.extractZipLine(line, state, partialTick);
            return;
        }
        LivingEntity target = entity instanceof StickyWeb web ? web.getHitEntity() : null;
        if (target != null) {
            this.extractOnEntity((StickyWeb) entity, target, state, partialTick);
        }
    }

    private void extractOnEntity(StickyWeb web, LivingEntity target, State state, float partialTick) {
        Vec3 feet = target.getPosition(partialTick);
        state.offset = feet.subtract(state.x, state.y, state.z);
        state.onEntity = true;
        state.in = Mth.lerp(Mth.lerp(partialTick, web.prevGrappleTimer, web.grappleTimer), 8.0F, -0.6F) / 16.0F;
        Minecraft mc = Minecraft.getInstance();
        boolean firstPerson = target == mc.player && mc.options.getCameraType().isFirstPerson();
        Vec3 hit = web.getHitOffset();
        double height = hit.y;
        float s = target.getBbWidth() / 0.6F;
        float scale = Mth.clamp(s, 0.5F, 1.5F);

        if (!firstPerson && StickyWeb.isOnHead(target, height)) {
            // on the head: flat on the face of the head box it hit, turning (and nodding) with the head
            state.bodyYaw = Mth.rotLerp(partialTick, target.yHeadRotO, target.yHeadRot);
            state.headPitch = Mth.lerp(partialTick, target.xRotO, target.getXRot());
            state.headPivot = StickyWeb.headPivot(target);
            float half = 0.25F * s + 0.01F;
            double y = Mth.clamp(height - state.headPivot, 0.12F * s, 0.4F * s);
            if (Math.abs(hit.z) >= Math.abs(hit.x)) {
                double sign = Math.signum(hit.z == 0 ? 1 : hit.z);
                state.local = new Vec3(Mth.clamp(hit.x * half, -half * 0.5, half * 0.5), y, sign * half);
                state.faceYaw = sign > 0 ? 0.0F : 180.0F;
            } else {
                double sign = Math.signum(hit.x);
                state.local = new Vec3(sign * half, y, Mth.clamp(hit.z * half, -half * 0.5, half * 0.5));
                state.faceYaw = sign > 0 ? 90.0F : -90.0F;
            }
            state.in = Mth.lerp(Mth.lerp(partialTick, web.prevGrappleTimer, web.grappleTimer), 8.0F, -0.2F) / 16.0F;
            state.w *= scale * 0.85F;
            return;
        }

        state.bodyYaw = Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot);
        // the web lies on the body's outline at the spot it hit (an ellipse: for humanoids the torso + arms are wide
        // and flat, the legs narrower), turned to face outwards from there
        float side;
        float front;
        if (StickyWeb.isHumanoid(target)) {
            if (height > target.getBbHeight() * 0.4F) {
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
            // webs on yourself in first person stay below the camera (chest / legs): seen when looking down; webs
            // on the head show as the web overlay on the screen instead
            height = Math.min(height, target.getEyeHeight() - 0.95);
        }
        state.local = new Vec3(hit.x * side, height, hit.z * front);
        state.faceYaw = (float) Math.toDegrees(Math.atan2(hit.x / side, hit.z / front));
        state.w *= firstPerson ? scale * 0.6F : scale;
    }

    private void extractZipLine(WebZipLine line, State state, float partialTick) {
        state.alpha = line.isDetached() ? Mth.clamp(1.0F - (line.ticksDetached + partialTick) / WebZipLine.FADE_TICKS, 0.0F, 1.0F) : 1.0F;
        state.w = Mth.lerp(Mth.lerp(partialTick, line.prevGrappleTimer, line.grappleTimer), 2.0F, 8.0F) / 32.0F;
        Entity hooked = line.getHooked();
        Entity shooter = line.getShooter();
        Vec3 origin = new Vec3(state.x, state.y, state.z);
        Vec3 end = origin.add(0.0, state.halfHeight, 0.0);
        if (hooked != null && shooter != null) {
            // on the hooked entity, on the side facing the shooter
            Vec3 pos = hooked.getPosition(partialTick);
            Vec3 toShooter = shooter.getPosition(partialTick).subtract(pos);
            double yaw = Math.atan2(toShooter.x, toShooter.z);
            float r = hooked.getBbWidth() / 2.0F + 0.05F;
            end = pos.add(Math.sin(yaw) * r, hooked.getBbHeight() / 2.0F, Math.cos(yaw) * r);
            state.onEntity = true;
            state.offset = end.subtract(origin).subtract(0.0, 0.0, 0.0);
            state.local = Vec3.ZERO;
            state.bodyYaw = 0.0F;
            state.faceYaw = (float) Math.toDegrees(yaw);
        }
        if (shooter instanceof LivingEntity living) {
            // Fisk's rope: from the anchor to the wrist (or, after letting go, to the falling loose end), sagging
            // when slack
            Vec3 dst = line.isDetached() ? line.prevEnd.lerp(line.end, partialTick) : handPosition(line, living, end, partialTick);
            float slack = 1.0F - Mth.lerp(partialTick, line.prevTension, line.tension);
            Vec3 mid;
            if (end.y > dst.y) {
                mid = new Vec3(end.x, Mth.lerp(slack * 2.0F, end.y, dst.y), end.z);
            } else {
                mid = new Vec3(dst.x, Mth.lerp(slack * 2.0F, dst.y, end.y), dst.z);
            }
            state.drawLine = true;
            state.segments = slack <= 0.001F ? 1 : 24;
            state.lineFrom = end.subtract(origin);
            state.lineMid = mid.subtract(origin);
            state.lineTo = dst.subtract(origin);
        }
    }

    /** Approximate world position of the shooter's wrist holding the line (from Fisk's web rope renderer). */
    private static Vec3 handPosition(WebZipLine line, LivingEntity shooter, Vec3 anchor, float pt) {
        Minecraft mc = Minecraft.getInstance();
        float k = line.getShootSide() == AbstractWebProjectile.LEFT ? -1.0F : 1.0F;
        if (shooter == mc.player && mc.options.getCameraType().isFirstPerson()) {
            Vec3 look = shooter.getViewVector(pt);
            Vec3 right = new Vec3(-look.z, 0.0, look.x);
            right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
            Vec3 up = right.cross(look);
            // on the line of sight through the aimed hand (measured on screen), turned with the arm while hanging
            org.joml.Vector3f hand = new org.joml.Vector3f(0.233F * k, -0.155F, -0.5F);
            org.joml.Quaternionf hang = line instanceof WebSwingLine sw && sw.isCaught() ? WebArmAnimation.firstPersonHangRotation(pt) : null;
            if (hang != null) {
                hang.transform(hand);
            }
            return shooter.getEyePosition(pt).add(right.scale(hand.x)).add(up.scale(hand.y)).add(look.scale(-hand.z));
        }
        float bodyYaw = Mth.rotLerp(pt, shooter.yBodyRotO, shooter.yBodyRot) * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Math.cos(bodyYaw), 0.0, -Math.sin(bodyYaw)).scale(k);
        if (line instanceof WebSwingLine swing && swing.isCaught() && swing.getHooked() == null) {
            // hanging: the wrist of the raised arm, with the same body rotation and arm pose as the model
            Vec3 wrist = WebZipAnimation.hangingWrist(shooter.getId(), pt, Mth.rotLerp(pt, shooter.yBodyRotO, shooter.yBodyRot),
                    shooter.getPosition(pt), k);
            if (wrist != null) {
                return wrist;
            }
        }
        double shoulderY = shooter.isCrouching() ? 1.2 : 1.41;
        Vec3 shoulder = shooter.getPosition(pt).add(0.0, shoulderY, 0.0).add(right.scale(5.0 / 16.0));
        Vec3 toAnchor = anchor.subtract(shoulder);
        if (toAnchor.lengthSqr() < 1.0E-4) {
            return shoulder;
        }
        return shoulder.add(toAnchor.normalize().scale(Math.min(10.0 / 16.0, toAnchor.length())));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int color = WebGeometry.color(state.alpha);
        int light = state.lightCoords;
        if (state.drawLine) {
            Vec3 from = state.lineFrom;
            Vec3 mid = state.lineMid;
            Vec3 to = state.lineTo;
            int segments = state.segments;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TEXTURE_ROPE),
                    (pose, consumer) -> WebGeometry.rope(pose, consumer, to, mid, from, segments, color, light));
        }

        poseStack.pushPose();
        if (state.onEntity) {
            // target's feet -> body (or head) space, forward = +Z -> spot on the body -> facing outwards
            poseStack.translate(state.offset.x, state.offset.y, state.offset.z);
            poseStack.rotateDegrees(Axis.YP, -state.bodyYaw);
            if (state.headPivot > 0.0F) {
                poseStack.translate(0.0F, state.headPivot, 0.0F);
                poseStack.rotateDegrees(Axis.XP, state.headPitch);
            }
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
        /** Head webs: the neck height and head pitch (0 pivot = on the body). */
        public float headPivot;
        public float headPitch;
        /** Spot on the body (or head) in its space (forward = +Z). */
        public Vec3 local = Vec3.ZERO;
        public float faceYaw;
        /** Web zip rope back to the shooter's wrist (quadratic curve through the sag point). */
        public boolean drawLine;
        public Vec3 lineFrom = Vec3.ZERO;
        public Vec3 lineMid = Vec3.ZERO;
        public int segments = 1;
        public Vec3 lineTo = Vec3.ZERO;
    }
}
