package lucraft.mods.heroes.speedsterheroes;

import lucraft.mods.heroes.speedsterheroes.commands.CommandSetExtraSpeedLevels;
import lucraft.mods.heroes.speedsterheroes.commands.CommandSetSpeedsterLevel;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.superpower.SuperpowerSpeedforce;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = SpeedsterHeroes.MODID, version = SpeedsterHeroes.VERSION, name = SpeedsterHeroes.NAME, dependencies = "required-before:lucraftcore@[1.10.2-1.1.1,)")
public class SpeedsterHeroes {

	public static final String MODID = "speedsterheroes";
	public static final String NAME = "SpeedsterHeroes";
	public static final String ASSETDIR = MODID + ":";
	public static final String VERSION = "1.11.2-1.1.2";

	@SidedProxy(clientSide = "lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesClientProxy", serverSide = "lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy")
	public static SpeedsterHeroesProxy proxy;
	
	@Instance(value = SpeedsterHeroes.MODID)
	public static SpeedsterHeroes instance;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		proxy.preInit(e);
	}
	
	public static Superpower speedforce = new SuperpowerSpeedforce();
	
	@EventHandler
	public void init(FMLInitializationEvent e) {
		proxy.init(e);
		
		SuperpowerHandler.registerSuperPower(speedforce);
	}
	
	@EventHandler
	public void init(FMLPostInitializationEvent e) {
		proxy.postInit(e);
	}
	
	@EventHandler
	public void init(FMLServerStartingEvent e) {
		ServerCommandManager commandManager = (ServerCommandManager) e.getServer().getCommandManager();
		commandManager.registerCommand(new CommandSetSpeedsterLevel());
		commandManager.registerCommand(new CommandSetExtraSpeedLevels());
	}
}
