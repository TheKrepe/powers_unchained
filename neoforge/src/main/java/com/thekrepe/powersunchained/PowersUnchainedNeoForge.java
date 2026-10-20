package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.command.SuitCommand;
import com.thekrepe.powersunchained.dev.CiServerHook;
import com.thekrepe.powersunchained.platform.NeoForgeRegistryHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Constants.MOD_ID)
public class PowersUnchainedNeoForge {

    public PowersUnchainedNeoForge(IEventBus modEventBus) {
        PowersUnchained.init();
        NeoForgeRegistryHelper.register(modEventBus);
        CiServerHook.register();
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> SuitCommand.register(event.getDispatcher()));
    }
}
