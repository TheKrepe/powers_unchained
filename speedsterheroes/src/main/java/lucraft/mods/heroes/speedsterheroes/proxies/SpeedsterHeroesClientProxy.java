package lucraft.mods.heroes.speedsterheroes.proxies;

import java.util.Calendar;
import java.util.Map;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.SHClientEvents;
import lucraft.mods.heroes.speedsterheroes.client.UpdateChecker;
import lucraft.mods.heroes.speedsterheroes.client.book.BookPagePhilosophersStone;
import lucraft.mods.heroes.speedsterheroes.client.book.BookPageSpeedster;
import lucraft.mods.heroes.speedsterheroes.client.render.LayerRendererSpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.client.render.SHRenderer;
import lucraft.mods.heroes.speedsterheroes.client.render.SpeedHUDRenderer;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderBlackFlash;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderLightning;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderRingDummy;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderSpeedMirage;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.client.render.entities.RenderTimeWraith;
import lucraft.mods.heroes.speedsterheroes.client.render.items.ItemRendererSuitRing;
import lucraft.mods.heroes.speedsterheroes.client.render.tileentities.TileEntityRendererParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.client.render.tileentities.TileEntityRendererParticleAcceleratorPart;
import lucraft.mods.heroes.speedsterheroes.client.render.tileentities.TileEntityRendererPhilosophersStoneChest;
import lucraft.mods.heroes.speedsterheroes.client.render.tileentities.TileEntityRendererSpeedforceDampener;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.entity.EntityLightning;
import lucraft.mods.heroes.speedsterheroes.entity.EntityRingDummy;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeWraith;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityParticleAcceleratorPart;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityPhilosophersStoneChest;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntitySpeedforceDampener;
import lucraft.mods.lucraftcore.client.gui.book.BookChapter;
import lucraft.mods.lucraftcore.client.gui.book.BookPage;
import lucraft.mods.lucraftcore.client.gui.book.BookPageChapterTitle;
import lucraft.mods.lucraftcore.client.gui.book.heroguide.BookHeroGuide;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedInventoryItemRendererRegistry;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class SpeedsterHeroesClientProxy extends SpeedsterHeroesProxy {

	public static boolean isChristmas = false;

	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);

		UpdateChecker.init();

		MinecraftForge.EVENT_BUS.register(new SHRenderer());
		MinecraftForge.EVENT_BUS.register(new SpeedHUDRenderer());
		MinecraftForge.EVENT_BUS.register(new SHClientEvents());

		Calendar calendar = Calendar.getInstance();

		if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
			isChristmas = true;
		}
	
		OBJLoader.INSTANCE.addDomain(SpeedsterHeroes.MODID);
		ModelLoader.setCustomModelResourceLocation(SHItems.savitarBlade, 0, new ModelResourceLocation(SpeedsterHeroes.MODID + ":" + "savitarBlade", "inventory"));	
		
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
		
		RenderingRegistry.registerEntityRenderingHandler(EntitySpeedMirage.class, new RenderSpeedMirage(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightning.class, new RenderLightning(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDimensionBreach.class, new RenderDimensionBreach(Minecraft.getMinecraft().getRenderManager()));
		// RenderingRegistry.registerEntityRenderingHandler(EntityParticleAcceleratorSit.class,
		// new
		// RenderParticleAcceleratorSit(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTimeRemnant.class, new RenderTimeRemnant(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTimeWraith.class, new RenderTimeWraith(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityRingDummy.class, new RenderRingDummy(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackFlash.class, new RenderBlackFlash(Minecraft.getMinecraft().getRenderManager()));

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityParticleAccelerator.class, new TileEntityRendererParticleAccelerator());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityParticleAcceleratorPart.class, new TileEntityRendererParticleAcceleratorPart());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpeedforceDampener.class, new TileEntityRendererSpeedforceDampener());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhilosophersStoneChest.class, new TileEntityRendererPhilosophersStoneChest());
		
		ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) SHItems.ring, new ItemRendererSuitRing());
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);

		addSpeedsterBookPages();
//		addArtifactsBookPages();
		
		Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
		RenderPlayer render;
		render = skinMap.get("default");
		render.addLayer(new LayerRendererSpeedsterHeroes(render));

		render = skinMap.get("slim");
		render.addLayer(new LayerRendererSpeedsterHeroes(render));
	}

	private void addSpeedsterBookPages() {
		BookChapter chapter = new BookChapter("speedsters", BookHeroGuide.guide);

		chapter.addPage(new BookPageChapterTitle(chapter));
		for (SpeedsterType type : SpeedsterType.speedsterTypes) {
			chapter.addPage(new BookPageSpeedster(chapter, type));
		}

		BookHeroGuide.guide.addChapter(chapter);
	}
	
	private void addArtifactsBookPages() {
		BookPage philosphersStone = new BookPagePhilosophersStone(BookHeroGuide.artifactsChapter);
		BookHeroGuide.artifactsChapter.addPage(philosphersStone);
	}

}
