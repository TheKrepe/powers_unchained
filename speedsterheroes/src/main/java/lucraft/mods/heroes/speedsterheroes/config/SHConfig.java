package lucraft.mods.heroes.speedsterheroes.config;

import java.util.HashMap;

import lucraft.mods.heroes.speedsterheroes.items.ItemSymbol;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class SHConfig {

	public static HashMap<Item, Boolean> recipes = new HashMap<Item, Boolean>();
	public static HashMap<SpeedsterType, Boolean> symbolRecipes = new HashMap<SpeedsterType, Boolean>();

	// General
	public static boolean frictionBurn;
	public static boolean velocity9Poison;
	public static boolean particleAcceleratorExplosion;

	// Client
	public static boolean trailRender;
	public static boolean flickerRender;
	public static boolean vibrateRender;
	public static boolean speedforceDampenerRender;
	public static boolean breachRender;
	public static boolean flickerSound;
	public static boolean vibrateSound;
	public static boolean useMph;
	public static boolean christmasEasterEgg;

	public static void preInit(FMLPreInitializationEvent e) {
		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();

		frictionBurn = config.getBoolean("Friction Burn", "General", true, "If enabled you'll get on fire while running without a suit");
		velocity9Poison = config.getBoolean("Velocity-9 Poison", "General", true, "If enabled you'll get poison and die when you use Velocity-9");
		particleAcceleratorExplosion = config.getBoolean("Particle Accelerator Explosion", "General", true, "If enabled the particle accelerator will explode after using it");

		trailRender = config.getBoolean("Trail Render", "Client", true, "If enabled player trails will render");
		flickerRender = config.getBoolean("Flicker Render", "Client", true, "If enabled player flickering will render");
		vibrateRender = config.getBoolean("Vibrate Render", "Client", true, "If enabled player vibration will render");
		speedforceDampenerRender = config.getBoolean("Speedforce Dampener Render", "Client", true, "If enabled the speedforce dampener will have extra render stuff");
		breachRender = config.getBoolean("Dimension Breach Render", "Client", true, "If enabled the dimension breach will render");
//		flickerSound = config.getBoolean("Flicker Sound", "Client", true, "If enabled flickering will have sound");
//		vibrateSound = config.getBoolean("Vibrating Sound", "Client", true, "If enabled vibrating will have sounds");
		useMph = config.getBoolean("Use mph", "Client", false, "If enabled mph will display instead of km/h");
		christmasEasterEgg = config.getBoolean("Christmas Easter Egg", "Client", true, "If enabled suits will look a bit different on christmas");
		
		for (Item items : SHItems.items) {
			recipes.put(items, config.get("Recipes", items.getUnlocalizedName().replace("item.", "").replace("tile.", ""), true).getBoolean());
		}

		for (SpeedsterType type : ItemSymbol.list) {
			symbolRecipes.put(type, config.get("Recipes", type.getUnlocalizedName() + "_symbol", true).getBoolean());
		}

//		for (SpeedsterAbility ab : SpeedsterAbility.abilities) {
//			abilities.put(ab, config.get("Abilities", ab.getUnlocalizedName(), true).getBoolean());
//		}

		config.save();
	}

}
