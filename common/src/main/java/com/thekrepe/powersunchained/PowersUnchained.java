package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUBlocks;
import com.thekrepe.powersunchained.registry.PUDataComponents;
import com.thekrepe.powersunchained.registry.PUCreativeTabs;
import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.registry.PUSounds;
import com.thekrepe.powersunchained.registry.PUItems;

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
        PUDataComponents.init();
        PUBlocks.init();
        PUItems.init();
        PUEntities.init();
        PUSounds.init();
        com.thekrepe.powersunchained.registry.PUMenus.init();
        PUCreativeTabs.init();
    }
}
