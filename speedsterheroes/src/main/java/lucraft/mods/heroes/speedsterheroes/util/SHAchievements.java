package lucraft.mods.heroes.speedsterheroes.util;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public class SHAchievements {

	public static AchievementPage speedsterAchievements;
	private static Achievement[] array;
	private static List<Achievement> list = new ArrayList<Achievement>();
	
	public static Achievement becomeSpeedster;
	public static Achievement frictionBurn;
	public static Achievement velocity9;
	public static Achievement velocity9Death;
	public static Achievement brahmastra;
	public static Achievement timeRemnant;
	public static Achievement phasing;
	public static Achievement suit;
	public static Achievement savitarBlade;
	public static Achievement blackFlash;
	public static Achievement becomeBlackFlash;
	public static Achievement timeWraith;
	public static Achievement suitRing;
	public static Achievement dimensionBreach;
	
	public static void preInit() {
		becomeSpeedster = (new Achievement("achievement.becomeSpeedster", "becomeSpeedster", 0, 0, new ItemStack(SHItems.iconItem), (Achievement)null)).setSpecial().registerStat();
		
		frictionBurn = (new Achievement("achievement.frictionBurn", "frictionBurn", -2, -1, new ItemStack(SHItems.iconItem, 1, 1), becomeSpeedster)).registerStat();
		suit = (new Achievement("achievement.suit", "suit", -4, -2, SHItems.getSymbolFromSpeedsterType(SpeedsterType.flashS2, 1), frictionBurn)).registerStat();
		timeRemnant = (new Achievement("achievement.timeRemnant", "timeRemnant", -4, -4, new ItemStack(SHItems.iconItem, 1, 4), suit)).registerStat();
		blackFlash = (new Achievement("achievement.blackFlash", "blackFlash", -2, -4, new ItemStack(SHItems.iconItem, 1, 3), timeRemnant)).registerStat();
		timeWraith = (new Achievement("achievement.timeWraith", "timeWraith", -6, -4, new ItemStack(SHItems.iconItem, 1, 5), timeRemnant)).registerStat();
		becomeBlackFlash = (new Achievement("achievement.becomeBlackFlash", "becomeBlackFlash", -2, -6, new ItemStack(SHItems.blackFlashHelmet), blackFlash)).registerStat();
		phasing = (new Achievement("achievement.phasing", "phasing", -5, -0, new ItemStack(SHItems.iconItem, 1, 2), suit)).registerStat();
		dimensionBreach = (new Achievement("achievement.dimensionBreach", "dimensionBreach", -7, -2, new ItemStack(SHItems.iconItem, 1, 6), suit)).registerStat();
		
		velocity9 = (new Achievement("achievement.velocity9", "velocity9", 3, -1, new ItemStack(SHItems.velocity9), becomeSpeedster)).registerStat();
		velocity9Death = (new Achievement("achievement.velocity9Death", "velocity9Death", 3, -3, new ItemStack(Items.SKULL, 1, 0), velocity9)).registerStat();
		
		brahmastra = (new Achievement("achievement.brahmastra", "brahmastra", 2, 2, new ItemStack(SHItems.philosophersStone), becomeSpeedster)).setSpecial().registerStat();
		savitarBlade = (new Achievement("achievement.savitarBlade", "savitarBlade", 4, 3, new ItemStack(SHItems.savitarBlade), brahmastra)).registerStat();
		
		suitRing = (new Achievement("achievement.suitRing", "suitRing", -1, 3, new ItemStack(SHItems.ring), becomeSpeedster)).registerStat();
		
		registerAchievement(becomeSpeedster);
		registerAchievement(velocity9);
		registerAchievement(frictionBurn);
		registerAchievement(brahmastra);
		registerAchievement(suit);
		registerAchievement(timeRemnant);
		registerAchievement(savitarBlade);
		registerAchievement(blackFlash);
		registerAchievement(phasing);
		registerAchievement(becomeBlackFlash);
		registerAchievement(timeWraith);
		registerAchievement(suitRing);
		registerAchievement(dimensionBreach);
		registerAchievement(velocity9Death);
	}
	
	public static void init() {
		array = new Achievement[list.size()];
		for(int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		
		speedsterAchievements = new AchievementPage(SpeedsterHeroes.NAME, array);
		
		AchievementPage.registerAchievementPage(speedsterAchievements);
	}
	
	public static void registerAchievement(Achievement ach) {
		if(!list.contains(ach))
			list.add(ach);
	}
	
}
