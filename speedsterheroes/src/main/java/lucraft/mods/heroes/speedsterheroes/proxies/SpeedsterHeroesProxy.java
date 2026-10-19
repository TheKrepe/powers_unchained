package lucraft.mods.heroes.speedsterheroes.proxies;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.SHAbilities;
import lucraft.mods.heroes.speedsterheroes.blocks.SHBlocks;
import lucraft.mods.heroes.speedsterheroes.client.gui.GuiHandler;
import lucraft.mods.heroes.speedsterheroes.client.sounds.SHSoundEvents;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach;
import lucraft.mods.heroes.speedsterheroes.entity.EntityLightning;
import lucraft.mods.heroes.speedsterheroes.entity.EntityParticleAcceleratorSit;
import lucraft.mods.heroes.speedsterheroes.entity.EntityRingDummy;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeWraith;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.network.SHPacketDispatcher;
import lucraft.mods.heroes.speedsterheroes.potions.PotionSpeedShock;
import lucraft.mods.heroes.speedsterheroes.potions.PotionVelocity9;
import lucraft.mods.heroes.speedsterheroes.recipes.SHRecipes;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.heroes.speedsterheroes.util.SHCommonEventHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpecialTrailPlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterAttributeModifiers;
import lucraft.mods.heroes.speedsterheroes.worldgen.WorldGenHinduTemple;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedsterHeroesProxy {

	public static Potion velocity9 = new PotionVelocity9();
	public static Potion speedShock = new PotionSpeedShock();
	
	public static CreativeTabs tabSpeedsterArmor = new CreativeTabs("tabSpeedsterArmor") {
		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() {
			return new ItemStack(SHItems.futureFlashHelmet);
		}

		@Override
		public boolean hasSearchBar() {
			return true;
		}

		public int getSearchbarWidth() {
			return 40;
		}

		@Override
		public String getBackgroundImageName() {
			return "speedsterSuitsTab.png";
		}
	};

	public static CreativeTabs tabSpeedster = new CreativeTabs("tabSpeedster") {
		@Override
		@SideOnly(Side.CLIENT)
		public ItemStack getTabIconItem() {
			return new ItemStack(SHItems.iconItem);
		}
	};
	
	public void preInit(FMLPreInitializationEvent e) {
		SpecialTrailPlayerHandler.init();
		SpeedsterType.preInit();
		SHItems.preInit();
		SHBlocks.preInit();
		SHAchievements.preInit();
		SpeedsterAttributeModifiers.preInit();
		SHConfig.preInit(e);
		SHAbilities.init();
		SHSoundEvents.init();

		NetworkRegistry.INSTANCE.registerGuiHandler(SpeedsterHeroes.instance, new GuiHandler());
		GameRegistry.registerWorldGenerator(new WorldGenHinduTemple(), 0);
		
		// Events
		MinecraftForge.EVENT_BUS.register(new SHCommonEventHandler());

		// Entities
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "lightning"), EntityLightning.class, "lightning", 52, SpeedsterHeroes.instance, 64, 4, true);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "dimension_breach"), EntityDimensionBreach.class, "dimensionBreach", 53, SpeedsterHeroes.instance, 64, 4, true);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "particel_accelerator_sit"), EntityParticleAcceleratorSit.class, "particleAcceleratorSit", 54, SpeedsterHeroes.instance, 64, 4, true);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "time_remnant"), EntityTimeRemnant.class, "timeRemnant", 55, SpeedsterHeroes.instance, 64, 4, true);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "time_wraith"), EntityTimeWraith.class, "timeWraith", 56, SpeedsterHeroes.instance, 64, 4, true, 0x666666, 0x393939);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "ring_dummy"), EntityRingDummy.class, "ringDummy", 57, SpeedsterHeroes.instance, 64, 4, true);
		EntityRegistry.registerModEntity(new ResourceLocation(SpeedsterHeroes.MODID, "black_flash"), EntityBlackFlash.class, "blackFlash", 58, SpeedsterHeroes.instance, 64, 1, true, 0x393939, 0xb50000);
		
		Potion.REGISTRY.register(42, new ResourceLocation(SpeedsterHeroes.MODID, "velocity"), velocity9);
		Potion.REGISTRY.register(43, new ResourceLocation(SpeedsterHeroes.MODID, "speedShock"), speedShock);
	}
	
	public void init(FMLInitializationEvent e) {
		SHRecipes.init();
		SHPacketDispatcher.registerPackets();
		SHAchievements.init();
		SpeedsterType.init();
	}
	
	public void postInit(FMLPostInitializationEvent e) {
		
	}
	
}
