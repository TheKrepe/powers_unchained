package com.thekrepe.powersunchained.web;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Client side: per entity, when it last fired a web from each wrist, the "aim" timers of both arms and the short
 * shot jab — the data of Fisk's web-shooter animations ({@code TICKS_SINCE_WEBSHOOT}, {@code WEB_AIM_*_TIMER},
 * {@code WEBSHOOTER_SHOOT_*}). Fed by the web projectiles when they appear on the client (shooter id + wrist), so it
 * works for every player in view without an extra packet. Rapid fire alternates wrists, so both arms stay up.
 */
public final class WebShotTracker {

    public static final int RIGHT = 0;
    public static final int LEFT = 1;

    /** An arm stays aimed while it fired less than this many ticks ago. */
    private static final int AIM_HOLD = 14;
    private static final float AIM_UP_TICKS = 2.5F;
    private static final float AIM_DOWN_TICKS = 7.0F;
    /** Length of the shot jab. */
    public static final int SHOT_TICKS = 4;

    private static final Map<Integer, State> STATES = new ConcurrentHashMap<>();

    private WebShotTracker() {
    }

    public static void onShot(int entityId, int side) {
        State state = STATES.computeIfAbsent(entityId, id -> new State());
        state.sinceShot[side] = 0;
        state.shotSide = side;
        state.shotAge = 0;
    }

    /** Keeps the arm aimed (e.g. while a web zip line pulls), without a new shot jab. */
    public static void hold(int entityId, int side) {
        STATES.computeIfAbsent(entityId, id -> new State()).sinceShot[side] = 0;
    }

    /** Client tick. */
    public static void tick() {
        Iterator<State> it = STATES.values().iterator();
        while (it.hasNext()) {
            State s = it.next();
            for (int side = 0; side < 2; side++) {
                if (s.sinceShot[side] < 255) {
                    s.sinceShot[side]++;
                }
                s.prevAim[side] = s.aim[side];
                if (s.sinceShot[side] < AIM_HOLD) {
                    s.aim[side] = Math.min(s.aim[side] + 1.0F / AIM_UP_TICKS, 1.0F);
                } else {
                    s.aim[side] = Math.max(s.aim[side] - 1.0F / AIM_DOWN_TICKS, 0.0F);
                }
            }
            if (s.shotAge <= SHOT_TICKS) {
                s.shotAge++;
            }
            if (s.aim[0] == 0 && s.prevAim[0] == 0 && s.aim[1] == 0 && s.prevAim[1] == 0 && s.shotAge > SHOT_TICKS) {
                it.remove();
            }
        }
    }

    public static @Nullable State get(int entityId) {
        return STATES.get(entityId);
    }

    public static final class State {
        final int[] sinceShot = {255, 255};
        final float[] aim = new float[2];
        final float[] prevAim = new float[2];
        int shotSide = RIGHT;
        int shotAge = SHOT_TICKS + 1;

        /** 0..1 how far the arm is raised to aim (interpolated). */
        public float aim(int side, float partialTick) {
            return this.prevAim[side] + (this.aim[side] - this.prevAim[side]) * partialTick;
        }

        /** 0..1 progress of the shot jab of the given arm, or -1 if none is playing. */
        public float shot(int side, float partialTick) {
            if (this.shotSide != side || this.shotAge > SHOT_TICKS) {
                return -1.0F;
            }
            return Math.min((this.shotAge + partialTick) / SHOT_TICKS, 1.0F);
        }
    }
}
