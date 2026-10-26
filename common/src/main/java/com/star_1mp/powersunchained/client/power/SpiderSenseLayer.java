package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.star_1mp.powersunchained.Constants;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Comic-book Spider-Sense: when it goes off, jagged bolts flash out round the head (each drawn on two crossed planes so
 * they show from every side), pop in, flicker, then fade. The local player's while it senses danger, other players'
 * from the server's alert.
 */
public class SpiderSenseLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier TEXTURE = Constants.id("textures/entity/power/spider_sense_bolt.png");
    /** Ticks a burst lasts. */
    private static final int BURST = 34;
    private static final int FULL_BRIGHT = 0xF000F0;
    /** Bolts: angle from straight up (degrees, + = right), length scale. */
    private static final float[][] BOLTS = {{-78, 0.85F}, {-50, 1.0F}, {-22, 0.8F}, {22, 0.8F}, {50, 1.0F}, {78, 0.85F}};

    private static final Map<Integer, Integer> BURSTS = new ConcurrentHashMap<>();
    private static int clock;

    public SpiderSenseLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    /** Starts (or restarts) the bolts over an entity's head. */
    public static void burst(int entityId) {
        BURSTS.put(entityId, clock);
    }

    /** From the server: another player's sense went off. */
    public static void onAlert(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getId() != entityId) {
            burst(entityId);
        }
    }

    public static void tick() {
        clock++;
        Iterator<Map.Entry<Integer, Integer>> it = BURSTS.entrySet().iterator();
        while (it.hasNext()) {
            if (clock - it.next().getValue() > BURST) {
                it.remove();
            }
        }
        if (Minecraft.getInstance().level == null) {
            BURSTS.clear();
        }
    }

    /** 0..1 strength of the bolts on an entity now (local player: also a soft glow while danger is still near). */
    private static float strength(int entityId, float pt) {
        float s = 0.0F;
        Integer start = BURSTS.get(entityId);
        if (start != null) {
            float t = clock - start + pt;
            float in = Mth.clamp(t / 3.0F, 0.0F, 1.0F);
            float out = Mth.clamp((BURST - t) / 10.0F, 0.0F, 1.0F);
            s = in * out;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            s = Math.max(s, 0.55F * SpiderSense.danger());
        }
        return s;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
        float pt = state.ageInTicks - Mth.floor(state.ageInTicks);
        float s = strength(state.id, pt);
        if (s <= 0.01F || state.isInvisible) {
            return;
        }
        float time = state.ageInTicks;
        // the middle of the (posed) head, then a camera-facing plane there: the bolts fan out round the head like
        // comic lines from every view, one clean quad each (crossed planes / double faces looked broken)
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        poseStack.translate(0.0F, -4.5F / 16.0F, 0.0F);
        Vector3f centre = poseStack.last().pose().transformPosition(0.0F, 0.0F, 0.0F, new Vector3f());
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.last().pose().identity().translate(centre).rotate(cameraOrientation);
        poseStack.last().normal().identity();
        float pop = 0.6F + 0.4F * Mth.clamp(s * 1.5F, 0.0F, 1.0F);
        for (int i = 0; i < BOLTS.length; i++) {
            float flicker = 0.7F + 0.3F * Mth.sin(time * 2.3F + i * 1.9F);
            int alpha = Mth.clamp(Math.round(255 * s * flicker), 0, 255);
            if (alpha < 8) {
                continue;
            }
            int color = alpha << 24 | 0xFFFFFF;
            float angle = (BOLTS[i][0] + Mth.sin(time * 3.1F + i) * 2.0F) * Mth.DEG_TO_RAD;
            boolean mirror = BOLTS[i][0] < 0.0F;  // the zigzag leans outwards on both sides
            float len = 0.4F * BOLTS[i][1] * pop;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TEXTURE),
                    (pose, consumer) -> bolt(pose.pose(), consumer, angle, len, color, mirror));
        }
        poseStack.popPose();
    }

    /** Camera orientation of the frame being drawn (set by the entity renderer mixin). */
    public static final org.joml.Quaternionf cameraOrientation = new org.joml.Quaternionf();

    /**
     * One bolt in the camera-facing plane (x right, y up): a quad along {@code angle} from straight up, from just
     * outside the head outwards; texture rows run tip (v 0) to root (v 1).
     */
    private static void bolt(Matrix4f m, VertexConsumer c, float angle, float len, int color, boolean mirror) {
        float dx = Mth.sin(angle);
        float dy = Mth.cos(angle);
        // across the bolt; the texture is 5x8 texels: keep its pixels square
        float w = len * 5.0F / 8.0F / 2.0F;
        float px = dy * w;
        float py = -dx * w;
        float r0 = 0.36F;
        float r1 = r0 + len;
        float ua = mirror ? 1.0F : 0.0F;
        float ub = 1.0F - ua;
        vertex(m, c, dx * r0 - px, dy * r0 - py, 0.0F, ua, 1.0F, color);
        vertex(m, c, dx * r0 + px, dy * r0 + py, 0.0F, ub, 1.0F, color);
        vertex(m, c, dx * r1 + px, dy * r1 + py, 0.0F, ub, 0.0F, color);
        vertex(m, c, dx * r1 - px, dy * r1 - py, 0.0F, ua, 0.0F, color);
    }

    private static void vertex(Matrix4f m, VertexConsumer c, float x, float y, float z, float u, float v, int color) {
        Vector3f p = m.transformPosition(x, y, z, new Vector3f());
        c.addVertex(p.x, p.y, p.z, color, u, v, OverlayTexture.NO_OVERLAY, FULL_BRIGHT, 0.0F, 1.0F, 0.0F);
    }
}
