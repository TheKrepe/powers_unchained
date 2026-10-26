package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
        poseStack.pushPose();
        this.getParentModel().head.translateAndRotate(poseStack);
        // head space: y down, 1/16 per pixel; the bolts start from the middle of the head
        poseStack.translate(0.0F, -4.5F / 16.0F, 0.0F);
        float pop = 0.6F + 0.4F * Mth.clamp(s * 1.5F, 0.0F, 1.0F);
        for (int plane = 0; plane < 2; plane++) {
            poseStack.pushPose();
            poseStack.rotateDegrees(Axis.YP, plane * 90.0F);
            for (int i = 0; i < BOLTS.length; i++) {
                float flicker = 0.7F + 0.3F * Mth.sin(time * 2.3F + i * 1.9F + plane);
                float jitter = Mth.sin(time * 3.1F + i) * 4.0F;
                int alpha = Mth.clamp(Math.round(255 * s * flicker), 0, 255);
                if (alpha < 8) {
                    continue;
                }
                int color = alpha << 24 | 0xFFFFFF;
                float angle = BOLTS[i][0] + jitter;
                float len = 0.34F * BOLTS[i][1] * pop;
                poseStack.pushPose();
                poseStack.rotateDegrees(Axis.ZP, angle);
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TEXTURE),
                        (pose, consumer) -> bolt(pose.pose(), consumer, len, color));
                poseStack.popPose();
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    /** One bolt: a quad pointing "up" (head space -y), from just outside the head outwards, both sides. */
    private static void bolt(Matrix4f m, VertexConsumer c, float len, int color) {
        float w = len / 6.0F;
        float y0 = -0.36F;
        float y1 = y0 - len;
        vertex(m, c, -w, y0, 0.0F, 0.0F, 1.0F, color);
        vertex(m, c, w, y0, 0.0F, 1.0F, 1.0F, color);
        vertex(m, c, w, y1, 0.0F, 1.0F, 0.0F, color);
        vertex(m, c, -w, y1, 0.0F, 0.0F, 0.0F, color);
        vertex(m, c, -w, y1, 0.0F, 0.0F, 0.0F, color);
        vertex(m, c, w, y1, 0.0F, 1.0F, 0.0F, color);
        vertex(m, c, w, y0, 0.0F, 1.0F, 1.0F, color);
        vertex(m, c, -w, y0, 0.0F, 0.0F, 1.0F, color);
    }

    private static void vertex(Matrix4f m, VertexConsumer c, float x, float y, float z, float u, float v, int color) {
        Vector3f p = m.transformPosition(x, y, z, new Vector3f());
        c.addVertex(p.x, p.y, p.z, color, u, v, OverlayTexture.NO_OVERLAY, FULL_BRIGHT, 0.0F, 1.0F, 0.0F);
    }
}
