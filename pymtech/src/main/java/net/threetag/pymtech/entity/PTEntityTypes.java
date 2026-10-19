package net.threetag.pymtech.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ObjectHolder;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.client.renderer.entity.DiscoTrailEntityRenderer;
import net.threetag.pymtech.client.renderer.entity.PymParticleDiskRenderer;
import net.threetag.pymtech.client.renderer.entity.ShrunkenStructureRenderer;

@ObjectHolder(PymTech.MODID)
@Mod.EventBusSubscriber(modid = PymTech.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PTEntityTypes {

    @ObjectHolder("disco_trail")
    public static final EntityType<DiscoTrailEntity> DISCO_TRAIL = null;

    @ObjectHolder("pym_particle_disk")
    public static final EntityType<PymParticleDiskEntity> PYM_PARTICLE_DISK = null;

    @ObjectHolder("shrunken_structure")
    public static final EntityType<ShrunkenStructureEntity> SHRUNKEN_STRUCTURE = null;

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> e) {
        e.getRegistry().register(EntityType.Builder.<DiscoTrailEntity>create(DiscoTrailEntity::new, EntityClassification.MISC).size(1F, 1F).setCustomClientFactory((spawnEntity, world) -> DISCO_TRAIL.create(world)).build(PymTech.MODID + ":disco_trail").setRegistryName("disco_trail"));
        e.getRegistry().register(EntityType.Builder.<PymParticleDiskEntity>create(PymParticleDiskEntity::new, EntityClassification.MISC).size(0.25F, 0.25F).setCustomClientFactory((spawnEntity, world) -> PYM_PARTICLE_DISK.create(world)).build(PymTech.MODID + ":pym_particle_disk").setRegistryName("pym_particle_disk"));
        e.getRegistry().register(EntityType.Builder.<ShrunkenStructureEntity>create(ShrunkenStructureEntity::new, EntityClassification.MISC).size(1F, 1F).setCustomClientFactory((spawnEntity, world) -> SHRUNKEN_STRUCTURE.create(world)).build(PymTech.MODID + ":shrunken_structure").setRegistryName("shrunken_structure"));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent e) {
        RenderingRegistry.registerEntityRenderingHandler(DISCO_TRAIL, DiscoTrailEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(PYM_PARTICLE_DISK, rendererManager -> new PymParticleDiskRenderer<>(rendererManager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(SHRUNKEN_STRUCTURE, ShrunkenStructureRenderer::new);
    }

}
