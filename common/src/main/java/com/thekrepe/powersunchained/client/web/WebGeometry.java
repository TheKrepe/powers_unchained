package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Vertex helpers for the web shots (from TheKrepe's Fisk's Superheroes port, used with permission). */
public final class WebGeometry {

    private WebGeometry() {
    }

    public static int color(float alpha) {
        int a = Math.round(Math.max(0.0F, Math.min(alpha, 1.0F)) * 255.0F);
        return a << 24 | 0xFFFFFF;
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int color,
            int light, float nx, float ny, float nz) {
        Matrix4f mat = pose.pose();
        Vector3f pos = mat.transformPosition(x, y, z, new Vector3f());
        Vector3f nrm = pose.normal().transform(new Vector3f(nx, ny, nz));
        consumer.addVertex(pos.x, pos.y, pos.z, color, u, v, OverlayTexture.NO_OVERLAY, light, nrm.x, nrm.y, nrm.z);
    }

    /** The four-leaved web shape: a star of {@code w} half-width whose centre is pushed back by {@code in}. */
    public static void webShape(PoseStack.Pose pose, VertexConsumer consumer, float w, float in, int color, int light,
            float nx, float ny, float nz) {
        float[][] verts = {
            {-w, 0, in, 0.0F, 0.5F}, {0, 0, 0, 0.5F, 0.5F}, {0, w, in, 0.5F, 0.0F}, {-w, w, in, 0.0F, 0.0F},
            {w, 0, in, 1.0F, 0.5F}, {w, w, in, 1.0F, 0.0F}, {0, w, in, 0.5F, 0.0F}, {0, 0, 0, 0.5F, 0.5F},
            {-w, 0, in, 0.0F, 0.5F}, {-w, -w, in, 0.0F, 1.0F}, {0, -w, in, 0.5F, 1.0F}, {0, 0, 0, 0.5F, 0.5F},
            {0, -w, in, 0.5F, 1.0F}, {w, -w, in, 1.0F, 1.0F}, {w, 0, in, 1.0F, 0.5F}, {0, 0, 0, 0.5F, 0.5F}
        };
        for (float[] v : verts) {
            vertex(pose, consumer, v[0], v[1], v[2], v[3], v[4], color, light, nx, ny, nz);
        }
    }
}
