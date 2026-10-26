package com.star_1mp.powersunchained.power;

import com.star_1mp.powersunchained.network.ClimbSyncPayload;
import com.star_1mp.powersunchained.platform.Services;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server side of wall climbing. The local client moves the climber (like web swinging) and reports where it clings
 * ({@link #MODE_NONE}, {@link #MODE_WALL} + the wall's direction, {@link #MODE_CEILING}); the server keeps the
 * fall distance at zero while climbing and passes the state on to everyone watching (for the climbing pose).
 */
public final class WallClimbing {

    public static final int MODE_NONE = 0;
    public static final int MODE_WALL = 1;
    public static final int MODE_CEILING = 2;
    /** Climbing over the top of a wall (for the pose). */
    public static final int MODE_MANTLE = 3;

    private static final Map<UUID, int[]> STATES = new HashMap<>();

    private WallClimbing() {
    }

    public static void update(ServerPlayer player, int mode, int direction) {
        if (mode != MODE_NONE && !Superpowers.isActive(player, com.star_1mp.powersunchained.power.PowerAbility.WALL_CLIMBING)) {
            mode = MODE_NONE;
        }
        int[] old = STATES.get(player.getUUID());
        if (mode == MODE_NONE) {
            STATES.remove(player.getUUID());
        } else {
            STATES.put(player.getUUID(), new int[]{mode, direction});
        }
        if (old == null ? mode != MODE_NONE : (old[0] != mode || old[1] != direction)) {
            Services.GAMEPLAY.sendToTrackingAndSelf(player, new ClimbSyncPayload(player.getId(), mode, direction));
        }
    }

    public static void stop(ServerPlayer player) {
        update(player, MODE_NONE, 0);
    }

    public static boolean isClimbing(ServerPlayer player) {
        return STATES.containsKey(player.getUUID());
    }

    static void serverTick(ServerPlayer player) {
        if (STATES.containsKey(player.getUUID())) {
            player.resetFallDistance();
        }
    }

    public static void forget(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }
}
