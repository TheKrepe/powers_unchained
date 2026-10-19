package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.render.entity.*;
import lucraft.mods.heroesexpansion.items.ItemHEArrow.ArrowType;
import lucraft.mods.heroesexpansion.items.ItemWebShooter;
import lucraft.mods.lucraftcore.infinity.render.RenderEntityInfinityStone;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEEntities {

    @SubscribeEvent
    public static void onRegisterEntities(RegistryEvent.Register<EntityEntry> e) {
        int id = 0;

        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityThrownThorWeapon.class).id(new ResourceLocation(HeroesExpansion.MODID, "thrown_mjolnir"), id++).name("thrown_mjolnir").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityThorWeapon.class).id(new ResourceLocation(HeroesExpansion.MODID, "mjolnir"), id++).name("mjolnir").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityTesseract.class).id(new ResourceLocation(HeroesExpansion.MODID, "tesseract"), id++).name("tesseract").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityPortal.class).id(new ResourceLocation(HeroesExpansion.MODID, "portal"), id++).name("portal").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityCaptainAmericaShield.class).id(new ResourceLocation(HeroesExpansion.MODID, "captain_america_shield"), id++).name("captain_america_shield").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityLeviathan.class).id(new ResourceLocation(HeroesExpansion.MODID, "leviathan"), id++).name("leviathan").tracker(60, 1, true).egg(0x657166, 0x654066).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityChitauri.class).id(new ResourceLocation(HeroesExpansion.MODID, "chitauri"), id++).name("chitauri").tracker(60, 1, true).egg(0x657166, 0x654066).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityKree.class).id(new ResourceLocation(HeroesExpansion.MODID, "kree"), id++).name("kree").tracker(60, 1, true).egg(0x006cff, 0x002353).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityVultureWings.class).id(new ResourceLocation(HeroesExpansion.MODID, "vulture_wings"), id++).name("vulture_wings").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityRadioactiveSpider.class).id(new ResourceLocation(HeroesExpansion.MODID, "radioactive_spider"), id++).name("radioactive_spider").tracker(60, 1, true).egg(0x0c294f, 0x632020).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityThrownBillyClub.class).id(new ResourceLocation(HeroesExpansion.MODID, "thrown_billy_club"), id++).name("thrown_billy_club").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityBillyClubHook.class).id(new ResourceLocation(HeroesExpansion.MODID, "billy_club_hook"), id++).name("billy_club_hook").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityBlackHole.class).id(new ResourceLocation(HeroesExpansion.MODID, "black_hole"), id++).name("black_hole").tracker(60, 1, true).build());
        e.getRegistry().register(EntityEntryBuilder.create().entity(EntityGrabbedBlock.class).id(new ResourceLocation(HeroesExpansion.MODID, "grabbed_block"), id++).name("grabbed_block").tracker(60, 1, true).build());

        for (ArrowType types : ArrowType.values()) {
            e.getRegistry().register(EntityEntryBuilder.create().entity(types.getEntityClass()).id(new ResourceLocation(HeroesExpansion.MODID, types.getName()), id++).name(types.getName()).tracker(60, 1, true).build());
        }

        for (ResourceLocation loc : ItemWebShooter.WEB_MODES.getKeys()) {
            ItemWebShooter.WebMode webMode = ItemWebShooter.WEB_MODES.getObject(loc);
            e.getRegistry().register(EntityEntryBuilder.create().entity(webMode.entityClass).id(loc, id++).name(loc.getPath()).tracker(60, 1, true).build());
        }
    }

    @SideOnly(Side.CLIENT)
    public static void loadRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownThorWeapon.class, RenderThrownThorWeapon::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityThorWeapon.class, RenderEntityInfinityStone::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityTesseract.class, RenderEntityTesseract::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, RenderPortal::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCaptainAmericaShield.class, RenderCaptainAmericaShield::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityLeviathan.class, RenderLeviathan::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityChitauri.class, RenderChitauri::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityKree.class, RenderKree::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityVultureWings.class, RenderVultureWings::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRadioactiveSpider.class, RenderRadioactiveSpider::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySound.class, RenderSound::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownBillyClub.class, RenderBillyClub::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBillyClubHook.class, RenderBillyClub::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, RenderBlackHole::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrabbedBlock.class, RenderGrabbedBlock::new);

        for (ArrowType types : ArrowType.values()) {
            registerArrowRenderer(types.getEntityClass(), RenderHEArrow.class, types);
        }

        for (ResourceLocation loc : ItemWebShooter.WEB_MODES.getKeys()) {
            ItemWebShooter.WebMode webMode = ItemWebShooter.WEB_MODES.getObject(loc);
            RenderingRegistry.registerEntityRenderingHandler(webMode.entityClass, RenderWebShoot::new);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerArrowRenderer(Class<? extends EntityArrow> entityClass, Class<? extends RenderHEArrow> renderer, ArrowType type) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, new IRenderFactory() {

            @Override
            public Render createRenderFor(RenderManager manager) {
                try {
                    return renderer.getConstructor(RenderManager.class, ArrowType.class).newInstance(manager, type);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

}
