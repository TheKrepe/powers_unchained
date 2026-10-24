package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Third person: where the players' wrists (the web-shooters) really are, taken from the drawn model (every pose,
 * the body rotations and the model scale included), so web lines start exactly at the hand. Stored as an offset from
 * the player's (interpolated) position; the lines read it on the next frame.
 */
public final class WebHandTracker {

    private static final Map<Integer, Hands> HANDS = new ConcurrentHashMap<>();
    /** Entity origin of the player being drawn right now (camera-relative), set when its render starts. */
    private static final Map<Integer, Vector3f> ORIGIN = new ConcurrentHashMap<>();

    private WebHandTracker() {
    }

    /** Start of a player's render: the pose stack is at the player's position (camera-relative, world axes). */
    public static void begin(int entityId, PoseStack poseStack) {
        ORIGIN.put(entityId, poseStack.last().pose().transformPosition(new Vector3f()));
    }

    /** A render layer of the player: the pose stack is in model space, the model posed. */
    public static void capture(PoseStack poseStack, PlayerModel model, AvatarRenderState state) {
        Vector3f origin = ORIGIN.get(state.id);
        if (origin == null) {
            return;
        }
        Matrix4f m = poseStack.last().pose();
        Hands hands = HANDS.computeIfAbsent(state.id, id -> new Hands());
        hands.right = wrist(m, model.rightArm, -1.0F).sub(origin);
        hands.left = wrist(m, model.leftArm, 1.0F).sub(origin);
        hands.time = System.nanoTime();
    }

    /** The web-shooter on the arm: near the end of the arm, in the middle of the arm's width. */
    private static Vector3f wrist(Matrix4f m, ModelPart arm, float side) {
        PoseStack stack = new PoseStack();
        stack.last().pose().set(m);
        arm.translateAndRotate(stack);
        return stack.last().pose().transformPosition(new Vector3f(side * 1.0F / 16.0F, 9.0F / 16.0F, 0.0F));
    }

    /** World position of the player's wrist (drawn within the last moment), or null. */
    public static @Nullable Vec3 wrist(int entityId, Vec3 renderPos, boolean right) {
        Hands hands = HANDS.get(entityId);
        if (hands == null || System.nanoTime() - hands.time > 200_000_000L) {
            return null;
        }
        Vector3f v = right ? hands.right : hands.left;
        return renderPos.add(v.x, v.y, v.z);
    }

    private static final class Hands {
        Vector3f right = new Vector3f();
        Vector3f left = new Vector3f();
        long time;
    }
}
