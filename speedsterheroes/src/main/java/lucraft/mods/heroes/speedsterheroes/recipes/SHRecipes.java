package lucraft.mods.heroes.speedsterheroes.recipes;

import lucraft.mods.heroes.speedsterheroes.blocks.SHBlocks;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.items.ItemSymbol;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.lucraftcore.items.LCItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class SHRecipes {

	public static void init() {
		RecipeSorter.register("speedsterheroes:tachyonChargeRecipe", ShapedTachyonChargeOreRecipe.class, Category.SHAPED, "after:minecraft:shaped before:minecraft:shapeless");
		
		// Particle Accelerator Control Block
		addRecipe(new ShapedOreRecipe(new ItemStack(SHBlocks.particleAccelerator, 1), "PRP", "RDR", "PRP", 'P', "blockIron", 'R', "dustRedstone", 'I', "ingotIron", 'D', "blockDiamond"));
		
		// Particle Accelerator Part Block
		addRecipe(new ShapedOreRecipe(new ItemStack(SHBlocks.particleAcceleratorPart, 3), "PIP", "SRB", "PXP", 'P', "blockIron", 'R', "blockRedstone", 'I', "plateIron", 'X', "platePalladium", 'S', "plateSilver", 'B', "plateBronze"));
		
		// Tachyon Prototype
		addRecipe(new ShapedTachyonChargeOreRecipe(new ItemStack(SHItems.tachyonPrototype), "IPI", "PCP", "IPI", 'P', "plateBronze", 'C', Blocks.GLOWSTONE, 'I', "ingotIron"));
		
		// Tachyon Device
		addRecipe(new ShapedTachyonChargeOreRecipe(new ItemStack(SHItems.tachyonDevice), "IPI", "PCP", "IPI", 'P', "plateBronze", 'C', SHItems.tachyonPrototype, 'I', "ingotIron"));
		
		// Tachyon Charge
		addRecipe(new ShapedOreRecipe(new ItemStack(SHItems.tachyonCharge), "NIN", "GXG", "NIN", 'I', "ingotIron", 'N', "nuggetIron", 'X', "blockPalladium", 'G', "dustGlowstone"));
		
		// Speedforce Dampener
		addRecipe(new ShapedOreRecipe(new ItemStack(SHBlocks.speedforceDampener), "PRP", "NCN", "PRP", 'P', "plateIron", 'R', "dustRedstone", 'N', "nuggetGold", 'C', SHItems.tachyonCharge));
		
		// Velocity 9
		addRecipe(new ShapelessOreRecipe(new ItemStack(SHItems.velocity9), new ItemStack(Items.POTIONITEM, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(SHItems.symbol, 1, OreDictionary.WILDCARD_VALUE), Items.BLAZE_POWDER, LCItems.syringe));
		
		// Suit Ring upgrade
		addRecipe(new ShapedOreRecipe(new ItemStack(SHItems.ringUpgrade), "NSN", "IPI", " I ", 'N', "nuggetGold", 'S', new ItemStack(SHItems.symbol, 1, OreDictionary.WILDCARD_VALUE), 'I', "ingotGold", 'P', "plateGold"));
		
		// --- Symbols ------------------------------------------------------------------------------------------------------
		
		// CW Flash S1
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.flashS1, 2), "GDG", "DPD", "GDG", 'P', "plateGold", 'G', "ingotGold", 'D', "dyeRed"));
		
		// CW Flash S2
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.flashS2, 2), "GDG", "DPD", "GDG", 'P', "plateGold", 'G', "ingotGold", 'D', "dyeWhite"));
		
		// CW Reverse Flash
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.reverseFlash, 2), "IDI", "DPD", "IDI", 'P', "plateIntertium", 'I', "ingotIntertium", 'D', "dyeBlack"));
		
		// CW Zoom
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.zoom, 2), "IDI", "DPD", "IDI", 'P', "plateDwarfStarAlloy", 'I', "ingotDwarfStarAlloy", 'D', "dyeBlack"));
		
		// CW Jay Garrick
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.jayGarrick, 2), " N", "NI", "II", 'N', "nuggetGold", 'I', "ingotGold"));
		
		// CW Jay Garrick 2
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.jayGarrick2, 2), "N ", "II", " N", 'N', "nuggetGold", 'I', "ingotGold"));
		
		// CW Kid Flash
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.kidFlash, 2), "IDI", "DPD", "IDI", 'P', "plateIntertium", 'I', "ingotIntertium", 'D', "dyeWhite"));
		
		// CW Rival
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.theRival, 2), "NO", "II", "BN", 'N', "nuggetGold", 'I', "ingotGold", 'O', "dyeOrange", 'B', "dyeBlack"));
	
		// CW Trajectory
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.trajectory, 2), "IPI", 'P', "plateGold", 'I', "ingotGold"));
	
		// CW Jesse Quick
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.jesseQuick, 2), "GDG", "DPD", "GDG", 'P', "plateIron", 'G', "ingotIntertium", 'D', "dyeWhite"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(SHItems.jesseQuickHelmet, SHItems.trajectoryHelmet, SHItems.getSymbolFromSpeedsterType(SpeedsterType.jesseQuick, 1)));
		GameRegistry.addRecipe(new ShapelessOreRecipe(SHItems.jesseQuickChestplate, SHItems.trajectoryChestplate, SHItems.getSymbolFromSpeedsterType(SpeedsterType.jesseQuick, 1)));
		GameRegistry.addRecipe(new ShapelessOreRecipe(SHItems.jesseQuickLegs, SHItems.trajectoryLegs, SHItems.getSymbolFromSpeedsterType(SpeedsterType.jesseQuick, 1)));
		GameRegistry.addRecipe(new ShapelessOreRecipe(SHItems.jesseQuickBoots, SHItems.trajectoryBoots, SHItems.getSymbolFromSpeedsterType(SpeedsterType.jesseQuick, 1)));
		
		// CW S.T.A.R. Labs Training Suit
		addSymbolRecipe(new ShapedOreRecipe(SHItems.getSymbolFromSpeedsterType(SpeedsterType.starLabsTraining, 1), "DBD", "BGB", "DBD", 'D', Items.DIAMOND, 'G', LCItems.heroGuide, 'B', "dyeWhite"));
		
//		addSymbolDungeonLoot(SpeedsterType.dceuFlash, 2, 4, 5);
//		addSymbolDungeonLoot(SpeedsterType.comicFlash, 2, 4, 3);
//		addSymbolDungeonLoot(SpeedsterType.rebirthWallyWest, 2, 4, 3);
//		addSymbolDungeonLoot(SpeedsterType.comicReverseFlash, 2, 4, 3);
//		addSymbolDungeonLoot(SpeedsterType.professorZoom, 2, 4, 3);
//		addSymbolDungeonLoot(SpeedsterType.new52Flash, 2, 4, 5);
//		addSymbolDungeonLoot(SpeedsterType.godspeed, 2, 4, 4);
//		addSymbolDungeonLoot(SpeedsterType.flay, 2, 4, 2);
	}
	
	public static void addSymbolDungeonLoot(SpeedsterType type, int i1, int i2, int i3) {
		if(SHConfig.symbolRecipes.get(type)) {
//			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo(ChestGenHooks.NETHER_FORTRESS).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
//			ChestGenHooks.getInfo("antman.quantumRealm").addItem(new WeightedRandomChestContent(SHItems.symbol, ItemSymbol.list.indexOf(type), i1, i2, i3));
		}
	}
	
	public static void addRecipe(IRecipe recipe) {
		if(SHConfig.recipes.containsKey(recipe.getRecipeOutput().getItem()) && SHConfig.recipes.get(recipe.getRecipeOutput().getItem())) {
			GameRegistry.addRecipe(recipe);
		}
	}
	
	public static void addSymbolRecipe(IRecipe recipe) {
		if(SHConfig.symbolRecipes.get( ((ItemSymbol)recipe.getRecipeOutput().getItem()).getSpeedsterType(recipe.getRecipeOutput()) )) {
			GameRegistry.addRecipe(recipe);
		}
	}
	
}
