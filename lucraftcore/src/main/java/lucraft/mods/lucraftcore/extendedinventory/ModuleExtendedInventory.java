package lucraft.mods.lucraftcore.extendedinventory;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.extendedinventory.abilitybar.ExtendedInventoryBarProvider;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.IExtendedInventoryCapability;
import lucraft.mods.lucraftcore.extendedinventory.gui.GuiHandlerEntryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageExtendedInventoryKey;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageOpenExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageSyncExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.render.LayerRendererExtendedInventory;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarHandler;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

public class ModuleExtendedInventory extends Module {

    public static final ModuleExtendedInventory INSTANCE = new ModuleExtendedInventory();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Capability Registering
        CapabilityManager.INSTANCE.register(IExtendedInventoryCapability.class, new CapabilityExtendedInventory.CapabilityExtendedInventoryStorage(), CapabilityExtendedInventory.class);

        // EventHandler Registering
        MinecraftForge.EVENT_BUS.register(new CapabilityExtendedInventory.CapabilityExtendedInventoryEventHandler());

        // Gui Handler
        LCGuiHandler.registerGuiHandlerEntry(GuiHandlerEntryExtendedInventory.ID, new GuiHandlerEntryExtendedInventory());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Packet Registering
        LCPacketDispatcher.registerMessage(MessageSyncExtendedInventory.Handler.class, MessageSyncExtendedInventory.class, Side.CLIENT, 60);
        LCPacketDispatcher.registerMessage(MessageOpenExtendedInventory.Handler.class, MessageOpenExtendedInventory.class, Side.SERVER, 61);
        LCPacketDispatcher.registerMessage(MessageExtendedInventoryKey.Handler.class, MessageExtendedInventoryKey.class, Side.SERVER, 62);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        // Ability Bar
        AbilityBarHandler.registerProvider(new ExtendedInventoryBarProvider());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initClient(FMLInitializationEvent event) {
        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        RenderPlayer render;
        render = skinMap.get("default");
        render.addLayer(new LayerRendererExtendedInventory(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerRendererExtendedInventory(render));
    }

    @Override
    public String getName() {
        return "ExtendedInventory";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.extended_inventory;
    }

}
