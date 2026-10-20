package com.thekrepe.powersunchained.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Client side: remembers who fired a web from which wrist and when, so the arm can be posed (pointed at the look
 * direction with a short jab). Fed by the web projectiles when they appear on the client — no extra packet.
 */
public final class WebShootAnimation {

    /** Length of the animation in ticks. */
    public static final float DURATION = 5.0F;

    private static final Map<Integer, Entry> ENTRIES = new ConcurrentHashMap<>();

    private WebShootAnimation() {
    }

    public static void trigger(int shooterId, int side, long gameTime) {
        ENTRIES.put(shooterId, new Entry(side, gameTime));
        if (ENTRIES.size() > 256) {
            ENTRIES.entrySet().removeIf(e -> gameTime - e.getValue().gameTime() > 40);
        }
    }

    public static @Nullable Entry get(int entityId) {
        return ENTRIES.get(entityId);
    }

    /**
     * Animation weight 0..1 (Fisk's curve {@code sin(pi * t^0.75)}): rises fast, falls back slower.
     *
     * @param time ticks since the shot (with partial tick)
     */
    public static float weight(float time) {
        if (time < 0 || time > DURATION) {
            return 0.0F;
        }
        return (float) Math.sin(Math.PI * Math.pow(time / DURATION, 0.75));
    }

    public record Entry(int side, long gameTime) {
    }
}
