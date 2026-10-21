package com.thekrepe.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
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

    // Note on lighting: the level's diffuse light directions are in world space and the entity pose stack has no camera
    // rotation, so the webs pass a fixed world "up" normal (not rotated with the web) and get the full daylight brightness
    // on every surface, still darkened by the lightmap at night / in caves. Rotating the normal with the web made webs
    // on walls and entities dark grey.
    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int color,
            int light) {
        Matrix4f mat = pose.pose();
        Vector3f pos = mat.transformPosition(x, y, z, new Vector3f());
        consumer.addVertex(pos.x, pos.y, pos.z, color, u, v, OverlayTexture.NO_OVERLAY, light, 0.0F, 1.0F, 0.0F);
    }

    /** The four-leaved web shape: a star of {@code w} half-width whose centre is pushed back by {@code in}. */
    public static void webShape(PoseStack.Pose pose, VertexConsumer consumer, float w, float in, int color, int light) {
        float[][] verts = {
            {-w, 0, in, 0.0F, 0.5F}, {0, 0, 0, 0.5F, 0.5F}, {0, w, in, 0.5F, 0.0F}, {-w, w, in, 0.0F, 0.0F},
            {w, 0, in, 1.0F, 0.5F}, {w, w, in, 1.0F, 0.0F}, {0, w, in, 0.5F, 0.0F}, {0, 0, 0, 0.5F, 0.5F},
            {-w, 0, in, 0.0F, 0.5F}, {-w, -w, in, 0.0F, 1.0F}, {0, -w, in, 0.5F, 1.0F}, {0, 0, 0, 0.5F, 0.5F},
            {0, -w, in, 0.5F, 1.0F}, {w, -w, in, 1.0F, 1.0F}, {w, 0, in, 1.0F, 0.5F}, {0, 0, 0, 0.5F, 0.5F}
        };
        for (float[] v : verts) {
            vertex(pose, consumer, v[0], v[1], v[2], v[3], v[4], color, light);
        }
    }

    /** A thin web line from {@code from} to {@code to}: two crossed strips (seen from every side). */
    public static void line(PoseStack.Pose pose, VertexConsumer consumer, Vec3 from, Vec3 to, int color, int light) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        if (len < 1.0E-4) {
            return;
        }
        Vec3 fwd = dir.scale(1.0 / len);
        Vec3 a = fwd.cross(new Vec3(0.0, 1.0, 0.0));
        a = a.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : a.normalize();
        Vec3 b = a.cross(fwd).normalize();
        float w = 0.02F;
        float v = 1.0F;
        for (Vec3 side : new Vec3[] {a.scale(w), b.scale(w)}) {
            Vec3 p0 = from.subtract(side);
            Vec3 p1 = from.add(side);
            Vec3 p2 = to.add(side);
            Vec3 p3 = to.subtract(side);
            vertex(pose, consumer, (float) p0.x, (float) p0.y, (float) p0.z, 0.0F, 0.0F, color, light);
            vertex(pose, consumer, (float) p1.x, (float) p1.y, (float) p1.z, 1.0F, 0.0F, color, light);
            vertex(pose, consumer, (float) p2.x, (float) p2.y, (float) p2.z, 1.0F, v, color, light);
            vertex(pose, consumer, (float) p3.x, (float) p3.y, (float) p3.z, 0.0F, v, color, light);
        }
    }

    /**
     * An axis-aligned box from {@code (x0, y0, z0)} to {@code (x1, y1, z1)}, every face tiled with the texture in
     * squares of {@code tile} blocks (the cocoon).
     */
    public static void tiledBox(PoseStack.Pose pose, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1,
            float z1, float tile, int color, int light) {
        // each face: origin corner, two edge vectors
        float[][] faces = {
            {x0, y0, z0, x1 - x0, 0, 0, 0, y1 - y0, 0},   // north (z0)
            {x1, y0, z1, x0 - x1, 0, 0, 0, y1 - y0, 0},   // south (z1)
            {x0, y0, z1, 0, 0, z0 - z1, 0, y1 - y0, 0},   // west (x0)
            {x1, y0, z0, 0, 0, z1 - z0, 0, y1 - y0, 0},   // east (x1)
            {x0, y1, z0, x1 - x0, 0, 0, 0, 0, z1 - z0},   // up
            {x0, y0, z1, x1 - x0, 0, 0, 0, 0, z0 - z1},   // down
        };
        for (float[] f : faces) {
            float lu = (float) Math.sqrt(f[3] * f[3] + f[4] * f[4] + f[5] * f[5]);
            float lv = (float) Math.sqrt(f[6] * f[6] + f[7] * f[7] + f[8] * f[8]);
            int nu = Math.max(1, (int) Math.ceil(lu / tile - 0.01));
            int nv = Math.max(1, (int) Math.ceil(lv / tile - 0.01));
            for (int i = 0; i < nu; i++) {
                for (int j = 0; j < nv; j++) {
                    float u0 = i * tile / lu;
                    float u1 = Math.min((i + 1) * tile / lu, 1.0F);
                    float v0 = j * tile / lv;
                    float v1 = Math.min((j + 1) * tile / lv, 1.0F);
                    float tu1 = (u1 - u0) * lu / tile;
                    float tv1 = (v1 - v0) * lv / tile;
                    float[][] corners = {{u0, v0, 0, 0}, {u1, v0, tu1, 0}, {u1, v1, tu1, tv1}, {u0, v1, 0, tv1}};
                    for (float[] c : corners) {
                        float x = f[0] + f[3] * c[0] + f[6] * c[1];
                        float y = f[1] + f[4] * c[0] + f[7] * c[1];
                        float z = f[2] + f[5] * c[0] + f[8] * c[1];
                        vertex(pose, consumer, x, y, z, c[2], 1.0F - c[3], color, light);
                    }
                }
            }
        }
    }
}
