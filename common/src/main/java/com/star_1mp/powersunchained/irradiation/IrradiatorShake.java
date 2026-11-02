package com.star_1mp.powersunchained.irradiation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client: entities being irradiated right now shake like a curing zombie villager (owner). Fed by the Irradiator block
 * entities' client tick (their synced target id), read by the living-entity renderer mixin.
 */
public final class IrradiatorShake {

    private static final Map<Integer, Long> SHAKING = new ConcurrentHashMap<>();

    private IrradiatorShake() {
    }

    public static void mark(int entityId, long gameTime) {
        if (SHAKING.size() > 64) {
            SHAKING.values().removeIf(t -> gameTime - t > 5);
        }
        SHAKING.put(entityId, gameTime);
    }

    public static boolean isShaking(int entityId, long gameTime) {
        Long t = SHAKING.get(entityId);
        return t != null && gameTime - t <= 2;
    }
}
