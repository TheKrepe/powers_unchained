package lucraft.mods.lucraftcore.karma;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.karma.capabilities.CapabilityKarma;
import lucraft.mods.lucraftcore.karma.capabilities.IKarmaCapability;
import lucraft.mods.lucraftcore.karma.commands.CommandKarma;
import lucraft.mods.lucraftcore.karma.network.MessageRequestKarmaSync;
import lucraft.mods.lucraftcore.karma.network.MessageSyncKarma;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

public class ModuleKarma extends Module {

    public static final ModuleKarma INSTANCE = new ModuleKarma();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Capability Registering
        CapabilityManager.INSTANCE.register(IKarmaCapability.class, new CapabilityKarma.Storage(), CapabilityKarma.class);

        // Event Handler
        MinecraftForge.EVENT_BUS.register(new CapabilityKarma.EventHandler());
        MinecraftForge.EVENT_BUS.register(new KarmaEventHandler());

        // Network
        LCPacketDispatcher.registerMessage(MessageSyncKarma.Handler.class, MessageSyncKarma.class, Side.CLIENT, 81);
        LCPacketDispatcher.registerMessage(MessageRequestKarmaSync.Handler.class, MessageRequestKarmaSync.class, Side.SERVER, 82);
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandKarma());
    }

    @Override
    public String getName() {
        return "Karma";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.karma;
    }

}
