package com.star_1mp.powersunchained.dev;

import com.star_1mp.powersunchained.Constants;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * CI helper (server side): with {@code -Dpowers_unchained.ci.server=true} the server stops as soon as the
 * world is generated. Used by the client test workflow to create a test world. Inactive otherwise.
 */
public final class CiServerHook {

    private CiServerHook() {
    }

    public static void register() {
        if (Boolean.getBoolean("powers_unchained.ci.server")) {
            Constants.LOG.warn("[CI] Server world generator enabled");
            NeoForge.EVENT_BUS.addListener(CiServerHook::onServerStarted);
        }
    }

    private static void onServerStarted(ServerStartedEvent event) {
        Constants.LOG.info("[CI] World generated, stopping server");
        event.getServer().halt(false);
    }
}
