package com.star_1mp.powersunchained.power;

import com.star_1mp.powersunchained.network.RollPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.web.WebShooting;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server side of the roll: the local client moves the roller (dash / flip impulse); the server checks the power and
 * tells everyone else watching, for the animation.
 */
public final class Rolling {

    public static final int KIND_ROLL = 0;
    public static final int KIND_FLIP = 1;

    private Rolling() {
    }

    public static void roll(ServerPlayer player, int kind, float yaw) {
        if (!Superpowers.isActive(player, Superpower.ROLL) || (kind != KIND_ROLL && kind != KIND_FLIP)) {
            return;
        }
        if (kind == KIND_FLIP) {
            // a flip off a jump or a web lands softly
            WebShooting.markFallSafe(player);
        }
        Services.GAMEPLAY.sendToTrackingAndSelf(player, new RollPayload(player.getId(), kind, yaw));
    }
}
