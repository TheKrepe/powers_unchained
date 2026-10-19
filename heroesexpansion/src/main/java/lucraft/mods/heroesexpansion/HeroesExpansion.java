package lucraft.mods.heroesexpansion;

import lucraft.mods.heroesexpansion.capabilities.CapabilityOnceInWorldStructures;
import lucraft.mods.heroesexpansion.capabilities.IOnceInWorldStructures;
import lucraft.mods.heroesexpansion.client.gui.HEGuiHandler;
import lucraft.mods.heroesexpansion.commands.CommandSolarEnergy;
import lucraft.mods.heroesexpansion.entities.EntityWebShoot;
import lucraft.mods.heroesexpansion.integration.HEFTBIntegration;
import lucraft.mods.heroesexpansion.integration.HEIEIntegration;
import lucraft.mods.heroesexpansion.integration.HETiConIntegration;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.items.ItemWebShooter;
import lucraft.mods.heroesexpansion.network.*;
import lucraft.mods.heroesexpansion.proxies.HECommonProxy;
import lucraft.mods.heroesexpansion.trigger.HECriteriaTriggers;
import lucraft.mods.heroesexpansion.worldgen.HEWorldGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = HeroesExpansion.MODID, version = HeroesExpansion.VERSION, name = HeroesExpansion.NAME, dependencies = HeroesExpansion.DEPENDENCIES)
public class HeroesExpansion {

    public static final String NAME = "HeroesExpansion";
    public static final String MODID = "heroesexpansion";
    public static final String VERSION = "1.12.2-1.3.5";
    public static final String DEPENDENCIES = "required-after:lucraftcore@[1.12.2-2.4.4,)";

    @SidedProxy(clientSide = "lucraft.mods.heroesexpansion.proxies.HEClientProxy", serverSide = "lucraft.mods.heroesexpansion.proxies.HECommonProxy")
    public static HECommonProxy proxy;

    @Instance(value = HeroesExpansion.MODID)
    public static HeroesExpansion INSTANCE;

    public static Logger LOGGER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        //SupporterHandler.enableSupporterCheck();

        // Web Modes
        ItemWebShooter.WEB_MODES.register(1, new ResourceLocation(HeroesExpansion.MODID, "pull_web"), new ItemWebShooter.WebMode(EntityWebShoot.EntityPullWeb.class, new TextComponentTranslation("heroesexpansion.web_mode.pull"), 1));
        ItemWebShooter.WEB_MODES.register(1, new ResourceLocation(HeroesExpansion.MODID, "swing_web"), new ItemWebShooter.WebMode(EntityWebShoot.EntitySwingWeb.class, new TextComponentTranslation("heroesexpansion.web_mode.swing"), 1));
        ItemWebShooter.WEB_MODES.register(2, new ResourceLocation(HeroesExpansion.MODID, "taser_web"), new ItemWebShooter.WebMode(EntityWebShoot.EntityTaserWeb.class, new TextComponentTranslation("heroesexpansion.web_mode.taser"), 2));
        ItemWebShooter.WEB_MODES.register(3, new ResourceLocation(HeroesExpansion.MODID, "impact_web"), new ItemWebShooter.WebMode(EntityWebShoot.EntityImpactWeb.class, new TextComponentTranslation("heroesexpansion.web_mode.impact"), 1));
        ItemWebShooter.WEB_MODES.register(4, new ResourceLocation(HeroesExpansion.MODID, "web_grenade"), new ItemWebShooter.WebMode(EntityWebShoot.EntityWebGrenade.class, new TextComponentTranslation("heroesexpansion.web_mode.web_grenade"), 2));

        // Proxy
        proxy.preInit(event);

        // Packets
        int id = 0;
        HEPacketDispatcher.registerMessage(MessageKineticEnergyBlast.Handler.class, MessageKineticEnergyBlast.class, Side.CLIENT, id++);
        HEPacketDispatcher.registerMessage(MessageOpenPortal.Handler.class, MessageOpenPortal.class, Side.SERVER, id++);
        HEPacketDispatcher.registerMessage(MessagePortalDevice.Handler.class, MessagePortalDevice.class, Side.SERVER, id++);
        HEPacketDispatcher.registerMessage(MessagePlayerChat.Handler.class, MessagePlayerChat.class, Side.CLIENT, id++);
        HEPacketDispatcher.registerMessage(MessageElytraFlying.Handler.class, MessageElytraFlying.class, Side.SERVER, id++);
        HEPacketDispatcher.registerMessage(MessageWebModesGUI.Handler.class, MessageWebModesGUI.class, Side.CLIENT, id++);
        HEPacketDispatcher.registerMessage(MessageChangeWebMode.Handler.class, MessageChangeWebMode.class, Side.SERVER, id++);
        HEPacketDispatcher.registerMessage(MessageSendInfoToClient.Handler.class, MessageSendInfoToClient.class, Side.CLIENT, id++);
        HEPacketDispatcher.registerMessage(MessageSendInfoToServer.Handler.class, MessageSendInfoToServer.class, Side.SERVER, id++);

        // Capability
        CapabilityManager.INSTANCE.register(IOnceInWorldStructures.class, new CapabilityOnceInWorldStructures.Storage(), CapabilityOnceInWorldStructures.class);

        // Mod Integration
        if (Loader.isModLoaded("ftbutilities"))
            MinecraftForge.EVENT_BUS.register(new HEFTBIntegration());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

        // Gui Handler
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new HEGuiHandler());

        // World Generator
        GameRegistry.registerWorldGenerator(new HEWorldGenerator(), 0);

        // Permissions
        HEPermissions.registerPermissions();

        // Mod Integration
        if (Loader.isModLoaded("tconstruct"))
            HETiConIntegration.init();
        if (Loader.isModLoaded("immersiveengineering"))
            HEIEIntegration.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        // Criteria Triggers
        HECriteriaTriggers.load();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSolarEnergy());
    }

    public static CreativeTabs CREATIVE_TAB = new CreativeTabs("tabHeroesExpansion") {

        @Override
        public ItemStack createIcon() {
            return new ItemStack(HEItems.CAPTAIN_AMERICA_SHIELD);
        }
    };

    static {
        FluidRegistry.enableUniversalBucket();
    }

}
