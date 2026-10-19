package lucraft.mods.lucraftcore.superpowers;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackConfig;
import lucraft.mods.lucraftcore.module.Module;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerItem;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuitSet;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.IAbilityProvider;
import lucraft.mods.lucraftcore.superpowers.abilitybar.SuperpowerAbilityBarProvider;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.superpowers.capabilities.ISuperpowerCapability;
import lucraft.mods.lucraftcore.superpowers.commands.CommandSuperpower;
import lucraft.mods.lucraftcore.superpowers.commands.CommandSuperpowerXP;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import lucraft.mods.lucraftcore.superpowers.entities.EntityEnergyBlast;
import lucraft.mods.lucraftcore.superpowers.items.SuperpowerItems;
import lucraft.mods.lucraftcore.superpowers.network.*;
import lucraft.mods.lucraftcore.superpowers.render.RenderEntityEnergyBlast;
import lucraft.mods.lucraftcore.superpowers.render.RenderEntityTrail;
import lucraft.mods.lucraftcore.superpowers.render.SuperpowerRenderLayer;
import lucraft.mods.lucraftcore.superpowers.render.SuperpowerRenderer;
import lucraft.mods.lucraftcore.superpowers.suitsets.RegisterSuitSetEvent;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.Map;

public class ModuleSuperpowers extends Module {

    public static final ModuleSuperpowers INSTANCE = new ModuleSuperpowers();

    public SuperpowerItems ITEMS = new SuperpowerItems();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        // Capability Registering
        CapabilityManager.INSTANCE.register(ISuperpowerCapability.class, new CapabilitySuperpower.CapabilitySuperpowerStorage(), CapabilitySuperpower.class);

        // EventHandler Registering
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ITEMS);
        MinecraftForge.EVENT_BUS.register(new CapabilitySuperpower.CapabilitySuperpowerEventHandler());

        // Permissions
        MinecraftForge.EVENT_BUS.register(new PermissionAbilityHandler());

        // SuitSets
        MinecraftForge.EVENT_BUS.post(new RegisterSuitSetEvent(SuitSet.REGISTRY));

        // Abilities
        Ability.registerSupplier(Ability.EnumAbilityContext.SUPERPOWER, (e) -> SuperpowerHandler.getSuperpower(e), (e, c) -> new AbilityContainerSuperpower(e));
        Ability.registerSupplier(Ability.EnumAbilityContext.SUIT, (e) -> SuitSet.getSuitSet(e), AbilityContainerSuitSet::new);
        Ability.registerSupplier(Ability.EnumAbilityContext.MAIN_HAND, (e) -> e.getHeldItemMainhand().getItem() instanceof IAbilityProvider ? (IAbilityProvider) e.getHeldItemMainhand().getItem() : null, (e, c) -> new AbilityContainerItem(e, c, EntityEquipmentSlot.MAINHAND));
        Ability.registerSupplier(Ability.EnumAbilityContext.OFF_HAND, (e) -> e.getHeldItemOffhand().getItem() instanceof IAbilityProvider ? (IAbilityProvider) e.getHeldItemOffhand().getItem() : null, (e, c) -> new AbilityContainerItem(e, c, EntityEquipmentSlot.OFFHAND));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // Packet Registering
        LCPacketDispatcher.registerMessage(MessageSyncSuperpower.Handler.class, MessageSyncSuperpower.class, Side.CLIENT, 40);
        LCPacketDispatcher.registerMessage(MessageAbilityKey.Handler.class, MessageAbilityKey.class, Side.SERVER, 41);
        LCPacketDispatcher.registerMessage(MessageToggleAbilityVisibility.Handler.class, MessageToggleAbilityVisibility.class, Side.SERVER, 42);
        LCPacketDispatcher.registerMessage(MessageSyncAbilityContainer.Handler.class, MessageSyncAbilityContainer.class, Side.CLIENT, 43);
        LCPacketDispatcher.registerMessage(MessageChooseSuperpowerGui.Handler.class, MessageChooseSuperpowerGui.class, Side.CLIENT, 44);
        LCPacketDispatcher.registerMessage(MessageChooseSuperpower.Handler.class, MessageChooseSuperpower.class, Side.SERVER, 45);
        LCPacketDispatcher.registerMessage(MessageSyncJsonSuperpower.Handler.class, MessageSyncJsonSuperpower.class, Side.CLIENT, 46);
        LCPacketDispatcher.registerMessage(MessageSyncJsonSuitSet.Handler.class, MessageSyncJsonSuitSet.class, Side.CLIENT, 47);

        // Injections
        ITEMS.loadSuperpowerInjection();

        // Permissions
        PermissionAbilityHandler.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SubscribeEvent
    public void onRegisterEntities(RegistryEvent.Register<EntityEntry> e) {
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityEnergyBlast.class).id(new ResourceLocation(LucraftCore.MODID, "energy_blast"), 0).name("energy_blast").tracker(64, 1, true).build());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void preInitClient(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EffectTrail.EntityTrail.class, RenderEntityTrail::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEnergyBlast.class, RenderEntityEnergyBlast::new);

        // Ability Bar Provider
        AbilityBarHandler.registerProvider(new SuperpowerAbilityBarProvider());
        MinecraftForge.EVENT_BUS.register(new SuperpowerRenderer());
    }

    @SuppressWarnings("deprecation")
    @SideOnly(Side.CLIENT)
    @Override
    public void initClient(FMLInitializationEvent event) {
        Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        RenderPlayer render;
        render = skinMap.get("default");
        render.addLayer(new SuperpowerRenderLayer(render));

        render = skinMap.get("slim");
        render.addLayer(new SuperpowerRenderLayer(render));

        // Abilities HTML file
        Ability.generateHtmlFile(new File(AddonPackConfig.addonpacksFolder + "/abilities.html"));
    }

    @Override
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandSuperpower());
        event.registerServerCommand(new CommandSuperpowerXP());
    }

    @Override
    public String getName() {
        return "Superpowers";
    }

    @Override
    public boolean isEnabled() {
        return LCConfig.modules.superpowers;
    }

}
