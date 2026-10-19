package lucraft.mods.lucraftcore.sizechanging;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.sizechanging.commands.CommandSize;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.network.MessageSyncSizeChanging;
import lucraft.mods.lucraftcore.sizechanging.render.RenderEntitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.render.SizeChangingRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleSizeChanging extends Module {

    public static final ModuleSizeChanging INSTANCE = new ModuleSizeChanging();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Capability Registering
        CapabilityManager.INSTANCE.register(ISizeChanging.class, new CapabilitySizeChanging.Storage(), CapabilitySizeChanging.class);

        // Event Handler
        MinecraftForge.EVENT_BUS.register(new CapabilitySizeChanging.EventHandler());
        MinecraftForge.EVENT_BUS.register(this);

        // Network
        LCPacketDispatcher.registerMessage(MessageSyncSizeChanging.Handler.class, MessageSyncSizeChanging.class, Side.CLIENT, 100);
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Override
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSize());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new SizeChangingRenderer());
        RenderingRegistry.registerEntityRenderingHandler(EntitySizeChanging.class, RenderEntitySizeChanging::new);
    }

    @Override
    public String getName() {
        return "SizeChanging";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.size_changing;
    }
}
