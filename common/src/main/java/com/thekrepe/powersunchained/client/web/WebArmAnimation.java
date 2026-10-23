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
 * Web-shooter arm animations. Same idea as Fisk's {@code web_aim} / {@code web_shoot} scripts (aim the wrist where you
 * look, a jab for every shot), with our own slightly different values: the arm sits a bit lower and closer to the
 * body, points a touch higher, the jab is shorter and kicks the wrist back with a small recoil:
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
            arm.x -= 0.8F * d;
            arm.y -= 0.7F * d;
            arm.zRot = Mth.lerp(d, arm.zRot, 0.06F - PI / 2);
            arm.xRot = Mth.lerp(d, arm.xRot, 0.14F - PI / 2 - headY);
            arm.yRot = rotLerp(d, arm.yRot, Math.min(headX + 0.16F, PI / 2));
        }
        float l = state.aim(WebShotTracker.LEFT, partialTick);
        if (l > 0) {
            float d = curve(l);
            ModelPart arm = model.leftArm;
            arm.x += 0.8F * d;
            arm.y -= 0.7F * d;
            arm.zRot = Mth.lerp(d, arm.zRot, PI / 2 - 0.06F);
            arm.xRot = Mth.lerp(d, arm.xRot, 0.14F - PI / 2 + headY);
            arm.yRot = rotLerp(d, arm.yRot, -Math.min(headX + 0.16F, PI / 2));
        }

        // web_shoot_right / web_shoot_left
        for (int side = 0; side < 2; side++) {
            float t = state.shot(side, partialTick);
            if (t < 0) {
                continue;
            }
            float d = curve((float) Math.sin(PI * Math.pow(t, 0.65)));
            float y0 = Mth.sin(headX - PI / 2);
            float f = 1.8F * d;
            ModelPart arm = side == WebShotTracker.RIGHT ? model.rightArm : model.leftArm;
            arm.x -= f * y0 * Mth.sin(headY);
            arm.z -= f * y0 * Mth.cos(headY);
            arm.y -= f * Mth.cos(headX - PI / 2) - d * 0.4F;
            arm.xRot -= 0.08F * d;  // the wrist kicks up a little (recoil)
        }
    }

    /** First person: 0..1 how far the given arm is raised to aim (0 = not shooting with it). */
    public static float firstPersonAim(HumanoidArm arm, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        WebShotTracker.State state = mc.player != null ? WebShotTracker.get(mc.player.getId()) : null;
        return state == null ? 0.0F : state.aim(arm == HumanoidArm.RIGHT ? WebShotTracker.RIGHT : WebShotTracker.LEFT, partialTick);
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
            float d = curve((float) Math.sin(PI * Math.pow(t, 0.65)));
            pose.translate(0.0F, -0.04F * d, 0.03F * d);
            pose.rotateDegrees(Axis.XP, 4.0F * d);
        }
        float a = state.aim(side, partial);
        if (a > 0) {
            float d = curve(a);
            pose.translate(m * -0.33F * d, -0.02F * d, 0.14F * d);
            pose.rotateDegrees(Axis.YP, m * -25.0F * d);
            pose.rotateDegrees(Axis.XP, -17.0F * d);
            pose.rotateDegrees(Axis.ZP, m * -28.0F * d);
            pose.translate(m * -0.4F * d, 0.0F, 0.0F);
            pose.rotateDegrees(Axis.YP, m * 147.0F * d);
            pose.rotateDegrees(Axis.ZP, m * -17.0F * d);
            pose.translate(m * 0.3F * d, 0.0F, 0.0F);
        }
    }
}
