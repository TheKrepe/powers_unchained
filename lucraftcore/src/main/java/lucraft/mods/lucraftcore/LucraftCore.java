package lucraft.mods.lucraftcore;

import lucraft.mods.lucraftcore.LCConfig.MessageSendModuleSettings;
import lucraft.mods.lucraftcore.LCConfig.MessageSyncConfig;
import lucraft.mods.lucraftcore.addonpacks.ModuleAddonPacks;
import lucraft.mods.lucraftcore.extendedinventory.ModuleExtendedInventory;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.karma.ModuleKarma;
import lucraft.mods.lucraftcore.materials.ModuleMaterials;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.proxies.LCCommonProxy;
import lucraft.mods.lucraftcore.sizechanging.ModuleSizeChanging;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.util.commands.CommandLocateExt;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent.MessagePlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import lucraft.mods.lucraftcore.util.items.OpenableArmor.MessageToggleArmor;
import lucraft.mods.lucraftcore.util.network.*;
import lucraft.mods.lucraftcore.util.triggers.LCCriteriaTriggers;
import lucraft.mods.lucraftcore.utilities.ModuleUtilities;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = LucraftCore.MODID, version = LucraftCore.VERSION, name = LucraftCore.NAME, dependencies = LucraftCore.DEPENDENCIES, acceptableRemoteVersions = "[1.12.2-2.3.3,)")
public class LucraftCore {

    public static final String NAME = "Lucraft: Core";
    public static final String MODID = "lucraftcore";
    public static final String VERSION = "1.12.2-2.4.16";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2838,);after:tconstruct";

    public static File config;

    @SidedProxy(clientSide = "lucraft.mods.lucraftcore.proxies.LCClientProxy", serverSide = "lucraft.mods.lucraftcore.proxies.LCCommonProxy")
    public static LCCommonProxy proxy;

    @Instance(value = LucraftCore.MODID)
    public static LucraftCore INSTANCE;

    public static ItemStack CREATIVE_TAB_ICON = new ItemStack(Blocks.BARRIER);

    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("tabLucraftCore") {

        @Override
        public ItemStack createIcon() {
            return CREATIVE_TAB_ICON;
        }

    };

    public static final RegistryNamespaced<String, Module> MODULES = new RegistryNamespaced<>();

    public static Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = event.getSuggestedConfigurationFile();

        // Packets
        LCPacketDispatcher.registerMessage(MessageSyncConfig.Handler.class, MessageSyncConfig.class, Side.CLIENT, 0);
        LCPacketDispatcher.registerMessage(MessageSendModuleSettings.Handler.class, MessageSendModuleSettings.class, Side.SERVER, 1);
        LCPacketDispatcher.registerMessage(MessagePlayerEmptyClickEvent.Handler.class, MessagePlayerEmptyClickEvent.class, Side.SERVER, 2);
        LCPacketDispatcher.registerMessage(MessageSyncPotionEffects.Handler.class, MessageSyncPotionEffects.class, Side.CLIENT, 3);
        LCPacketDispatcher.registerMessage(MessageToggleArmor.Handler.class, MessageToggleArmor.class, Side.SERVER, 4);
        LCPacketDispatcher.registerMessage(MessageSwingArm.Handler.class, MessageSwingArm.class, Side.CLIENT, 5);
        LCPacketDispatcher.registerMessage(MessageSpawnParticle.Handler.class, MessageSpawnParticle.class, Side.CLIENT, 6);
        LCPacketDispatcher.registerMessage(MessageAccelerating.Handler.class, MessageAccelerating.class, Side.SERVER, 7);
        LCPacketDispatcher.registerMessage(MessageTurn.Handler.class, MessageTurn.class, Side.SERVER, 8);

        // Gui Handler
        NetworkRegistry.INSTANCE.registerGuiHandler(LucraftCore.INSTANCE, new LCGuiHandler());

        // Supporter Handler
        SupporterHandler.load();

        registerModule(ModuleUtilities.INSTANCE);
        registerModule(ModuleAddonPacks.INSTANCE);
        if (LCConfig.modules.materials)
            registerModule(ModuleMaterials.INSTANCE);
        if (LCConfig.modules.superpowers)
            registerModule(ModuleSuperpowers.INSTANCE);
        if (LCConfig.modules.extended_inventory)
            registerModule(ModuleExtendedInventory.INSTANCE);
        if (LCConfig.modules.karma)
            registerModule(ModuleKarma.INSTANCE);
        if (LCConfig.modules.infinity)
            registerModule(ModuleInfinity.INSTANCE);
        //if (LCConfig.modules.superpowers && LCConfig.modules.advanced_combat)
        //    registerModule(ModuleAdvancedCombat.INSTANCE);
        if (LCConfig.modules.size_changing)
            registerModule(ModuleSizeChanging.INSTANCE);

        MODULES.forEach(t -> {
            LOGGER.info("Module '" + t.getName() + "' started pre-initialization");
            t.preInit(event);
        });

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MODULES.forEach(t -> {
            LOGGER.info("Module '" + t.getName() + "' started initialization");
            t.init(event);
        });

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MODULES.forEach(t -> {
            LOGGER.info("Module '" + t.getName() + "' started post-initialization");
            t.postInit(event);
        });

        proxy.postInit(event);

        // Criteria Triggers
        LCCriteriaTriggers.init();
    }

    @EventHandler
    public void init(FMLServerStartingEvent e) {
        if (LCConfig.overrideLocateCommand)
            e.registerServerCommand(new CommandLocateExt());

        MODULES.forEach(t -> t.onServerStarting(e));
    }

    private static int moduleId = 0;

    private static void registerModule(Module module) {
        MODULES.register(moduleId, module.getName(), module);
        LOGGER.info("Registered module '" + module.getName() + "'");
        moduleId++;
    }

}
