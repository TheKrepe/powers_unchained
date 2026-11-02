package com.star_1mp.powersunchained;

import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.registry.PUBlocks;
import com.star_1mp.powersunchained.registry.PUDataComponents;
import com.star_1mp.powersunchained.registry.PUCreativeTabs;
import com.star_1mp.powersunchained.registry.PUEntities;
import com.star_1mp.powersunchained.registry.PUSounds;
import com.star_1mp.powersunchained.registry.PUItems;

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
        com.star_1mp.powersunchained.registry.PUBlockEntities.init();
        com.star_1mp.powersunchained.registry.PUEffects.init();
        PUSounds.init();
        com.star_1mp.powersunchained.registry.PUMenus.init();
        PUCreativeTabs.init();
    }
}
