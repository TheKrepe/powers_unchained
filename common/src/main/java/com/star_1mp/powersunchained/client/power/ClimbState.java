package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.star_1mp.powersunchained.power.WallClimbing;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/**
 * How climbers look: every player clinging to a wall or a ceiling (the local one from {@link WallClimbClient},
 * others from the server's sync) gets a body orientation that turns smoothly towards the surface — facing the wall,
 * or lying belly-up under the ceiling — with the head pointing where it crawls, plus a spider-like crawl of the
 * limbs driven by the distance moved. Letting go turns the body back upright the same smooth way.
 */
public final class ClimbState {

    private static final Map<Integer, Entry> ENTRIES = new ConcurrentHashMap<>();
    private static final float DEG = Mth.DEG_TO_RAD;
    /** Body centre's height over the feet when upright. */
    private static final double CENTER = 0.9;
    /** Body centre's distance below the ceiling (the limbs reach up to it). */
    private static final double CEILING_DROP = 0.42;
    /** Body centre's shift towards a wall (the hitbox's half width keeps it a little off). */
    private static final double WALL_SHIFT = 0.04;
    private static final float TURN = 0.38F;

    private ClimbState() {
    }

    /** Server sync for other players (the local player's own state comes from {@link WallClimbClient}). */
    public static void onSync(int entityId, int mode, int direction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getId() == entityId) {
            return;
        }
        set(entityId, mode, Direction.from3DDataValue(direction));
    }

    private static void set(int entityId, int mode, Direction wall) {
        Entry e = ENTRIES.get(entityId);
        if (e == null) {
            if (mode == WallClimbing.MODE_NONE) {
                return;
            }
            e = new Entry();
            ENTRIES.put(entityId, e);
        }
        e.mode = mode;
        if (mode == WallClimbing.MODE_WALL) {
            e.wall = wall.getAxis().isHorizontal() ? wall : Direction.NORTH;
        }
    }

    /** Dev/CI only: poses an entity (e.g. a mannequin) as climbing, its head towards {@code heading}, mid-crawl. */
    public static void debugSet(int entityId, int mode, Direction wall, Vec3 heading) {
        set(entityId, mode, wall);
        Entry e = ENTRIES.get(entityId);
        if (e != null) {
            e.debugHeading = heading.normalize();
        }
    }

    public static boolean isClimbing(int entityId) {
        Entry e = ENTRIES.get(entityId);
        return e != null && e.mode != WallClimbing.MODE_NONE;
    }

    /** Whether the entity's climbing pose is (still partly) shown. */
    public static boolean isPosed(int entityId) {
        return ENTRIES.containsKey(entityId);
    }

    /** Client tick: orientations turn towards their targets, crawl cycles advance. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ENTRIES.clear();
            WallClimbClient.reset();
            return;
        }
        if (mc.player != null) {
            set(mc.player.getId(), WallClimbClient.isClimbing() ? WallClimbClient.mode() : WallClimbing.MODE_NONE,
                    WallClimbClient.wall());
        }
        Iterator<Map.Entry<Integer, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Entry> me = it.next();
            Entity entity = mc.level.getEntity(me.getKey());
            if (!(entity instanceof LivingEntity living) || !entity.isAlive()) {
                it.remove();
                continue;
            }
            if (me.getValue().tick(living)) {
                it.remove();
            }
        }
    }

    // ---- whole body ----

    /**
     * Render: right after the body yaw rotation (frame: y up, model forward = -Z), turns the body into its climbing
     * orientation.
     */
    public static void applyBody(PoseStack poseStack, int entityId, float partialTick, float scale, float bodyYaw) {
        Entry e = ENTRIES.get(entityId);
        if (e == null || e.cur == null) {
            return;
        }
        // back to world axes, then the climbing orientation around the body's centre
        poseStack.rotateDegrees(Axis.YP, -(180.0F - bodyYaw));
        Vec3 off = e.prevOffset.lerp(e.offset, partialTick);
        poseStack.translate(off.x * scale, off.y * scale, off.z * scale);
        poseStack.rotate(new Quaternionf(e.prev).slerp(e.cur, partialTick));
        poseStack.translate(0.0, -CENTER * scale, 0.0);
    }

    // ---- limbs ----

    /** Spider crawl: arms reaching ahead and onto the surface, legs splayed onto it, alternating as it moves. */
    public static void applyParts(PlayerModel m, int entityId, float partialTick) {
        Entry e = ENTRIES.get(entityId);
        if (e == null) {
            return;
        }
        float d = Mth.lerp(partialTick, e.prevWeight, e.weight);
        if (d <= 0.0F) {
            return;
        }
        d = d * d * (3.0F - 2.0F * d);
        float phase = Mth.lerp(partialTick, e.prevPhase, e.phase);
        float amp = Mth.lerp(partialTick, e.prevAmp, e.amp);
        float c = Mth.sin(phase) * amp;
        float lift = Math.max(0.0F, Mth.cos(phase)) * amp;
        float lift2 = Math.max(0.0F, -Mth.cos(phase)) * amp;
        // wide frog stance: hands up and out, knees (legs) splayed far to the sides, reaching onto the surface
        rot(m.rightArm, -142.0F + 30.0F * c, 10.0F, 40.0F + 10.0F * lift, d);
        rot(m.leftArm, -142.0F - 30.0F * c, -10.0F, -40.0F - 10.0F * lift2, d);
        rot(m.rightLeg, -58.0F - 22.0F * c, 8.0F, 38.0F + 8.0F * lift2, d);
        rot(m.leftLeg, -58.0F + 22.0F * c, -8.0F, -38.0F - 8.0F * lift, d);
        rot(m.body, 4.0F, 3.0F * c, 0.0F, d);
        rot(m.head, -38.0F, 0.0F, -4.0F * c, d);
    }

    private static void rot(ModelPart p, float x, float y, float z, float d) {
        p.xRot = Mth.lerp(d, p.xRot, x * DEG);
        p.yRot = Mth.lerp(d, p.yRot, y * DEG);
        p.zRot = Mth.lerp(d, p.zRot, z * DEG);
    }

    // ---- state ----

    private static final class Entry {
        int mode = WallClimbing.MODE_NONE;
        Direction wall = Direction.NORTH;
        @Nullable Quaternionf cur;
        Quaternionf prev = new Quaternionf();
        Vec3 offset = new Vec3(0.0, CENTER, 0.0);
        Vec3 prevOffset = this.offset;
        /** Where the head points while crawling (world, unit). */
        @Nullable Vec3 heading;
        float weight;
        float prevWeight;
        float phase;
        float prevPhase;
        float amp;
        float prevAmp;
        @Nullable Vec3 lastPos;
        @Nullable Vec3 debugHeading;

        /** @return whether it is done (upright again) */
        boolean tick(LivingEntity entity) {
            Vec3 pos = entity.position();
            Vec3 moved = this.lastPos == null ? Vec3.ZERO : pos.subtract(this.lastPos);
            this.lastPos = pos;
            boolean climbing = this.mode != WallClimbing.MODE_NONE;

            // head direction: along the surface, where it moves
            Vec3 n = this.mode == WallClimbing.MODE_CEILING ? new Vec3(0.0, 1.0, 0.0) : normal(this.wall);
            Vec3 along = moved.subtract(n.scale(moved.dot(n)));
            if (this.debugHeading != null) {
                this.heading = this.debugHeading;
                along = this.debugHeading.scale(0.1);
            } else if (climbing) {
                if (along.lengthSqr() > 4.0E-4) {
                    this.heading = along.normalize();
                } else if (this.heading != null) {
                    this.heading = this.heading.subtract(n.scale(this.heading.dot(n)));
                    this.heading = this.heading.lengthSqr() > 1.0E-3 ? this.heading.normalize() : null;
                }
                if (this.heading == null) {
                    this.heading = this.mode == WallClimbing.MODE_CEILING ? facing(entity) : new Vec3(0.0, 1.0, 0.0);
                }
            }

            Quaternionf target;
            Vec3 targetOffset;
            if (!climbing) {
                target = basis(facing(entity), new Vec3(0.0, 1.0, 0.0));
                targetOffset = new Vec3(0.0, CENTER, 0.0);
            } else if (this.mode == WallClimbing.MODE_CEILING) {
                target = basis(new Vec3(0.0, 1.0, 0.0), this.heading);
                targetOffset = new Vec3(0.0, entity.getBbHeight() - CEILING_DROP, 0.0);
            } else {
                target = basis(n, this.heading);
                targetOffset = new Vec3(0.0, CENTER, 0.0).add(n.scale(WALL_SHIFT));
            }
            if (this.cur == null) {
                this.cur = basis(facing(entity), new Vec3(0.0, 1.0, 0.0));
            }
            this.prev = new Quaternionf(this.cur);
            this.cur = new Quaternionf(this.cur).slerp(target, TURN);
            this.prevOffset = this.offset;
            this.offset = this.offset.lerp(targetOffset, TURN);

            this.prevWeight = this.weight;
            this.weight = Mth.approach(this.weight, climbing ? 1.0F : 0.0F, 0.2F);
            this.prevPhase = this.phase;
            this.prevAmp = this.amp;
            double speed = along.length();
            this.phase += (float) Math.min(speed, 0.4) * 4.2F;
            this.amp = Mth.approach(this.amp, climbing && speed > 0.01 ? 1.0F : 0.0F, 0.2F);

            if (climbing) {
                return false;
            }
            return this.weight <= 0.0F && this.cur.angle() >= 0.0F && angleBetween(this.cur, target) < 2.0F * DEG
                    && this.offset.distanceToSqr(targetOffset) < 1.0E-4;
        }
    }

    private static Vec3 facing(LivingEntity entity) {
        float yaw = entity.yBodyRot * DEG;
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    private static Vec3 normal(Direction d) {
        return new Vec3(d.getStepX(), d.getStepY(), d.getStepZ());
    }

    /** Rotation taking the model (right +X, up +Y, forward -Z) to {@code forward} / {@code up} in the world. */
    private static Quaternionf basis(Vec3 forward, Vec3 up) {
        Vec3 f = forward.normalize();
        Vec3 u = up.subtract(f.scale(up.dot(f)));
        if (u.lengthSqr() < 1.0E-6) {
            u = Math.abs(f.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(0.0, 0.0, 1.0);
            u = u.subtract(f.scale(u.dot(f)));
        }
        u = u.normalize();
        Vec3 r = f.cross(u);
        Matrix3f m = new Matrix3f(
                (float) r.x, (float) r.y, (float) r.z,
                (float) u.x, (float) u.y, (float) u.z,
                (float) -f.x, (float) -f.y, (float) -f.z);
        return new Quaternionf().setFromNormalized(m);
    }

    private static float angleBetween(Quaternionf a, Quaternionf b) {
        float dot = Math.abs(a.dot(b));
        return 2.0F * (float) Math.acos(Math.min(1.0F, dot));
    }
}
