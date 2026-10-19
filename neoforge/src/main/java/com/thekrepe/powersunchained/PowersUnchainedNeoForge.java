package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.dev.CiServerHook;
import com.thekrepe.powersunchained.platform.NeoForgeRegistryHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class PowersUnchainedNeoForge {

    public PowersUnchainedNeoForge(IEventBus modEventBus) {
        PowersUnchained.init();
        NeoForgeRegistryHelper.register(modEventBus);
        CiServerHook.register();
    }
}
