package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thekrepe.powersunchained.web.WebShotTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Fisk's web-shooter animations, translated from the original {@code .fsk} scripts:
 * <ul>
 *   <li>{@code web_aim_right/left}: the arm that fired is raised and pointed where the player looks (held while
 *       firing, lowered after ~0.7 s); with rapid fire both arms are up.</li>
 *   <li>{@code web_shoot_right/left}: a short jab of the arm along the look direction for every shot.</li>
 *   <li>First person (right arm, mirrored for a left main arm): the arm swings in front of the camera, pointing
 *       forward, plus a small dip for every shot.</li>
 * </ul>
 */
public final class WebArmAnimation {

    private static final float PI = Mth.PI;

    private WebArmAnimation() {
    }

    /** Fisk's {@code curve}: ease in-out. */
    private static float curve(float f) {
        return (float) ((Math.sin((f * 2.0 - 1.0) * Math.PI / 2.0) + 1.0) / 2.0);
    }

    private static float rotLerp(float delta, float from, float to) {
        float d = to - from;
        while (d < -PI) {
            d += 2 * PI;
        }
        while (d >= PI) {
            d -= 2 * PI;
        }
        return from + delta * d;
    }

    /** Third person: called after the vanilla pose of any player-shaped model. */
    public static void apply(HumanoidModel<?> model, int entityId, float partialTick) {
        WebShotTracker.State state = WebShotTracker.get(entityId);
        if (state == null) {
            return;
        }
        float headX = model.head.xRot;
        float headY = model.head.yRot;

        // web_aim_right / web_aim_left
        float r = state.aim(WebShotTracker.RIGHT, partialTick);
        if (r > 0) {
            float d = curve(r);
            ModelPart arm = model.rightArm;
            arm.x -= d;
            arm.y -= d;
            arm.zRot = Mth.lerp(d, arm.zRot, -PI / 2);
            arm.xRot = Mth.lerp(d, arm.xRot, 0.1F - PI / 2 - headY);
            arm.yRot = rotLerp(d, arm.yRot, Math.min(headX + 0.1F, PI / 2));
        }
        float l = state.aim(WebShotTracker.LEFT, partialTick);
        if (l > 0) {
            float d = curve(l);
            ModelPart arm = model.leftArm;
            arm.x += d;
            arm.y -= d;
            arm.zRot = Mth.lerp(d, arm.zRot, PI / 2);
            arm.xRot = Mth.lerp(d, arm.xRot, 0.1F - PI / 2 + headY);
            arm.yRot = rotLerp(d, arm.yRot, -Math.min(headX + 0.1F, PI / 2));
        }

        // web_shoot_right / web_shoot_left
        for (int side = 0; side < 2; side++) {
            float t = state.shot(side, partialTick);
            if (t < 0) {
                continue;
            }
            float d = curve((float) Math.sin(PI * Math.pow(t, 0.75)));
            float y0 = Mth.sin(headX - PI / 2);
            float f = 2 * d;
            ModelPart arm = side == WebShotTracker.RIGHT ? model.rightArm : model.leftArm;
            arm.x -= f * y0 * Mth.sin(headY);
            arm.z -= f * y0 * Mth.cos(headY);
            arm.y -= f * Mth.cos(headX - PI / 2) - d / 2;
        }
    }

    /** First person: transforms the arm being drawn (pose stack of the arm render event). */
    public static void applyFirstPerson(PoseStack pose, HumanoidArm arm) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        WebShotTracker.State state = WebShotTracker.get(mc.player.getId());
        if (state == null) {
            return;
        }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean right = arm == HumanoidArm.RIGHT;
        int side = right ? WebShotTracker.RIGHT : WebShotTracker.LEFT;
        float m = right ? 1.0F : -1.0F;  // mirror for a left main arm

        float t = state.shot(side, partial);
        if (t >= 0) {
            float d = curve((float) Math.sin(PI * Math.pow(t, 0.75)));
            pose.translate(0.0F, -0.05F * d, 0.0F);
        }
        float a = state.aim(side, partial);
        if (a > 0) {
            float d = curve(a);
            pose.translate(m * -0.35F * d, 0.0F, 0.15F * d);
            pose.rotateDegrees(Axis.YP, m * -27.0F * d);
            pose.rotateDegrees(Axis.XP, -20.0F * d);
            pose.rotateDegrees(Axis.ZP, m * -30.0F * d);
            pose.translate(m * -0.4F * d, 0.0F, 0.0F);
            pose.rotateDegrees(Axis.YP, m * 150.0F * d);
            pose.rotateDegrees(Axis.ZP, m * -20.0F * d);
            pose.translate(m * 0.3F * d, 0.0F, 0.0F);
        }
    }
}
