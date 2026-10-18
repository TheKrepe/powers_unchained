package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.platform.Services;

/**
 * Loader-independent entry point. Both the NeoForge and Fabric entry points call {@link #init()}.
 * Everything that does not need a loader-specific API lives in the common module.
 */
public final class PowersUnchained {

    private PowersUnchained() {
    }

    public static void init() {
        Constants.LOG.info("{} is loading on {} ({} environment)",
                Constants.MOD_NAME,
                Services.PLATFORM.getPlatformName(),
                Services.PLATFORM.getEnvironmentName());
    }
}
