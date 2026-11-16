package com.star_1mp.powersunleashed;

import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.registry.PUBlocks;
import com.star_1mp.powersunleashed.registry.PUDataComponents;
import com.star_1mp.powersunleashed.registry.PUCreativeTabs;
import com.star_1mp.powersunleashed.registry.PUEntities;
import com.star_1mp.powersunleashed.registry.PUSounds;
import com.star_1mp.powersunleashed.registry.PUItems;

/**
 * Loader-independent entry point. Both the NeoForge and Fabric entry points call {@link #init()}.
 * Everything that does not need a loader-specific API lives in the common module.
 */
public final class PowersUnleashed {

    private PowersUnleashed() {
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
        com.star_1mp.powersunleashed.registry.PUBlockEntities.init();
        com.star_1mp.powersunleashed.registry.PUEffects.init();
        PUSounds.init();
        com.star_1mp.powersunleashed.registry.PUMenus.init();
        PUCreativeTabs.init();
    }
}
