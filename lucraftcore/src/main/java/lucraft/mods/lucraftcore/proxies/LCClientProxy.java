package lucraft.mods.lucraftcore.proxies;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.ModuleExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.gui.InventoryTabExtendedInventory;
import lucraft.mods.lucraftcore.karma.ModuleKarma;
import lucraft.mods.lucraftcore.karma.gui.InventoryTabKarma;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.superpowers.gui.InventoryTabSuitSetAbilities;
import lucraft.mods.lucraftcore.superpowers.gui.InventoryTabSuperpowerAbilities;
import lucraft.mods.lucraftcore.tabs.GCTabWrapper;
import lucraft.mods.lucraftcore.tabs.InventoryTabVanilla;
import lucraft.mods.lucraftcore.tabs.TabRegistry;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarKeys;
import lucraft.mods.lucraftcore.util.items.OpenableArmor;
import lucraft.mods.lucraftcore.util.particles.ParticleColoredCloud;
import lucraft.mods.lucraftcore.util.render.LayerRendererAprilFools;
import lucraft.mods.lucraftcore.util.updatechecker.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
public class LCClientProxy extends LCCommonProxy {

    public static int COOLDOWN_TIMER = 0;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        // Openable Armor
        OpenableArmor.init();

        LucraftCore.MODULES.forEach(new Consumer<Module>() {
            @Override
            public void accept(Module t) {
                t.preInitClient(event);
            }
        });
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // Ability Bar Keys
        new AbilityBarKeys();

        // Particles
        ParticleManager pm = Minecraft.getMinecraft().effectRenderer;
        pm.registerParticle(ParticleColoredCloud.ID, new ParticleColoredCloud.Factory());

        // UpdateChecker
        if (LCConfig.updateChecker)
            new UpdateChecker(LucraftCore.VERSION, TextFormatting.DARK_GRAY + "[" + TextFormatting.WHITE + "Lucraft: Core" + TextFormatting.DARK_GRAY + "]", "http://mods.curse.com/mc-mods/minecraft/230651-lucraft-core", "https://drive.google.com/uc?export=download&id=0B6_wwPkl6fmOVFhpcmxrVmxCbWM");

        // April Fools
        if (LayerRendererAprilFools.isAprilFoolsDay()) {
            LayerRendererAprilFools.download();
            Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((s, r) -> r.addLayer(new LayerRendererAprilFools(r)));
        }

        LucraftCore.MODULES.forEach(t -> t.initClient(event));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        LucraftCore.MODULES.forEach(new Consumer<Module>() {
            @Override
            public void accept(Module t) {
                t.postInitClient(event);
            }
        });

        // Inventory Tabs
        if (TabRegistry.GC_API_LOADED) {
            initGC();
        } else {
            if (TabRegistry.getTabList().isEmpty()) {
                MinecraftForge.EVENT_BUS.register(new TabRegistry());
                TabRegistry.registerTab(new InventoryTabVanilla());
            }

            if (ModuleSuperpowers.INSTANCE.isEnabled()) {
                TabRegistry.registerTab(new InventoryTabSuperpowerAbilities());
                TabRegistry.registerTab(new InventoryTabSuitSetAbilities());
            }

            if (ModuleExtendedInventory.INSTANCE.isEnabled())
                TabRegistry.registerTab(new InventoryTabExtendedInventory());

            if (ModuleKarma.INSTANCE.isEnabled())
                TabRegistry.registerTab(new InventoryTabKarma());
        }
    }

    public static void initGC() {
        GCTabWrapper.init();
    }

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft() : super.getThreadFromContext(ctx));
    }

    @Override
    public void spawnParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(particleId, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
    }

    public static boolean canClickTab() {
        if (COOLDOWN_TIMER == 0) {
            COOLDOWN_TIMER = 10;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent e) {
        if (COOLDOWN_TIMER > 0)
            COOLDOWN_TIMER--;
    }

}
