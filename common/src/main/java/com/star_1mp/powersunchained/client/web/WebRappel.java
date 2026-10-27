package com.star_1mp.powersunchained.client.web;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * Upside down on the web (Fisk's Superheroes' web rappel): hanging still on a swing rope, holding sneak flips the
 * player head-down, hanging by the feet; the mouse wheel then lets the rope out / pulls it in. This keeps the flip
 * timer of every player (the local one from {@link WebSwingClient}, others from the server) and draws the pose:
 * Fisk's {@code web_rappel} animation — the body turned over around its middle, arms folded in front, legs crossed
 * round the rope — and the upside-down camera.
 */
public final class WebRappel {

    /** Ticks to turn over (Fisk: 8). */
    private static final float FLIP_TICKS = 8.0F;
    private static final Map<Integer, Entry> ENTRIES = new ConcurrentHashMap<>();

    private static final class Entry {
        boolean on;
        float timer;
        float prev;
    }

    private WebRappel() {
    }

    /** Sets whether an entity hangs upside down now. */
    public static void set(int entityId, boolean on) {
        Entry e = ENTRIES.get(entityId);
        if (e == null) {
            if (!on) {
                return;
            }
            e = new Entry();
            ENTRIES.put(entityId, e);
        }
        e.on = on;
    }

    /** From the server: another player turned over / back. */
    public static void onSync(int entityId, boolean on) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getId() != entityId) {
            set(entityId, on);
        }
    }

    /** The raw flip timer (0..1) of the entity, this tick. */
    public static float timer(int entityId) {
        Entry e = ENTRIES.get(entityId);
        return e == null ? 0.0F : e.timer;
    }

    public static void tick() {
        Iterator<Entry> it = ENTRIES.values().iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            e.prev = e.timer;
            e.timer = Mth.approach(e.timer, e.on ? 1.0F : 0.0F, 1.0F / FLIP_TICKS);
            if (!e.on && e.timer <= 0.0F && e.prev <= 0.0F) {
                it.remove();
            }
        }
        if (Minecraft.getInstance().level == null) {
            ENTRIES.clear();
        }
    }

    /** Fisk's {@code curve}: an S-shaped ease (sine). */
    private static float curve(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return (Mth.sin((t * 2.0F - 1.0F) * Mth.PI / 2.0F) + 1.0F) / 2.0F;
    }

    /** 0..1 how far the entity is turned over (eased, interpolated). */
    public static float weight(int entityId, float pt) {
        Entry e = ENTRIES.get(entityId);
        return e == null ? 0.0F : curve(Mth.lerp(pt, e.prev, e.timer));
    }

    /** Camera roll of the local player (degrees): upside down with the body. */
    public static float cameraRoll(float pt) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null ? 0.0F : 180.0F * weight(mc.player.getId(), pt);
    }

    /** Render, after the body yaw (y up): the body turns over around its middle (Fisk: pivot 0.5, 180° about Z). */
    public static void applyBody(PoseStack poseStack, int entityId, float pt, float scale) {
        float d = weight(entityId, pt);
        if (d <= 0.0F) {
            return;
        }
        float py = 0.5F * 1.8F * scale;
        poseStack.translate(0.0F, py, 0.0F);
        poseStack.rotate(new Quaternionf().rotationZ(180.0F * d * Mth.DEG_TO_RAD));
        poseStack.translate(0.0F, -py, 0.0F);
    }

    /** Limbs: Fisk's {@code web_rappel} pose (arms folded in front, legs crossed round the rope, the head looking "up"). */
    public static void applyParts(PlayerModel m, int entityId, float pt) {
        float d = weight(entityId, pt);
        if (d <= 0.0F) {
            return;
        }
        // the head keeps looking where the player looks, the right way up
        m.head.xRot = Mth.lerp(d, m.head.xRot, -m.head.xRot + 0.4F);
        m.head.yRot = rotLerp(d, m.head.yRot, -m.head.yRot);
        m.body.xRot -= 0.5F * d;

        m.rightArm.x -= 0.5F * d;
        m.rightArm.z -= 0.2F * d;
        to(m.rightArm, -0.5F, -1.2F, 0.0F, d);
        m.leftArm.x += 0.5F * d;
        m.leftArm.z -= 0.2F * d;
        to(m.leftArm, -0.5F, 1.2F, 0.0F, d);

        m.rightLeg.x -= 5.0F * d;
        m.rightLeg.y -= d;
        m.rightLeg.z -= 7.0F * d;
        to(m.rightLeg, 0.4F, 1.0F, -0.2F, d);
        m.leftLeg.x += 5.0F * d;
        m.leftLeg.y -= d;
        m.leftLeg.z -= 7.0F * d;
        to(m.leftLeg, 0.4F, -1.0F, 0.2F, d);
    }

    /** Blends a part's angles (radians) towards x / y (angle-wise) / z. */
    private static void to(ModelPart p, float x, float y, float z, float d) {
        p.xRot = Mth.lerp(d, p.xRot, x);
        p.yRot = rotLerp(d, p.yRot, y);
        p.zRot = Mth.lerp(d, p.zRot, z);
    }

    private static float rotLerp(float d, float from, float to) {
        float diff = to - from;
        while (diff < -Mth.PI) {
            diff += (Mth.PI * 2.0F);
        }
        while (diff >= Mth.PI) {
            diff -= (Mth.PI * 2.0F);
        }
        return from + d * diff;
    }
}
