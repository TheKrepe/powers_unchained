package lucraft.mods.heroes.speedsterheroes.items;

import java.util.ArrayList;

import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice.TachyonDeviceType;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SHItems {

	public static Item iconItem;
	
	public static Item velocity9;
	public static Item tachyonCharge;
	public static Item tachyonPrototype;
	public static Item tachyonDevice;
	public static Item smallTachyonDevice;
	public static Item symbol;
	public static Item ring;
	public static Item ringUpgrade;
	public static Item philosophersStone;
	public static Item savitarBlade;
	
	public static Item flashS1Helmet, flashS1Chestplate, flashS1Legs, flashS1Boots;
	public static Item flashHelmet, flashChestplate, flashLegs, flashBoots;
	public static Item futureFlashHelmet, futureFlashChestplate, futureFlashLegs, futureFlashBoots;
	public static Item reverseFlashHelmet, reverseFlashChestplate, reverseFlashLegs, reverseFlashBoots;
	public static Item zoomHelmet, zoomChestplate, zoomLegs, zoomBoots;
	public static Item jayGarrickHelmet, jayGarrickChestplate, jayGarrickLegs, jayGarrickBoots;
	public static Item jayGarrick2Chestplate, jayGarrick2Legs, jayGarrick2Boots;
	public static Item kidFlashHelmet, kidFlashChestplate, kidFlashLegs, kidFlashBoots;
	public static Item theRivalHelmet, theRivalChestplate, theRivalLegs, theRivalBoots;
	public static Item trajectoryHelmet, trajectoryChestplate, trajectoryLegs, trajectoryBoots;
	public static Item jesseQuickHelmet, jesseQuickChestplate, jesseQuickLegs, jesseQuickBoots;
	public static Item savitarHelmet, savitarChestplate, savitarLegs, savitarBoots;
	public static Item blackFlashHelmet, blackFlashChestplate, blackFlashLegs, blackFlashBoots;
	public static Item acceleratedManHelmet, acceleratedManChestplate, acceleratedManLegs, acceleratedManBoots;
	public static Item dceuFlashHelmet, dceuFlashChestplate, dceuFlashLegs, dceuFlashBoots;
	public static Item comicFlashHelmet, comicFlashChestplate, comicFlashLegs, comicFlashBoots;
	public static Item comicReverseFlashHelmet, comicReverseFlashChestplate, comicReverseFlashLegs, comicReverseFlashBoots;
	public static Item professorZoomHelmet, professorZoomChestplate, professorZoomLegs, professorZoomBoots;
	public static Item new52FlashHelmet, new52FlashChestplate, new52FlashLegs, new52FlashBoots;
	public static Item wallyWestComicFlashHelmet, wallyWestComicFlashChestplate, wallyWestComicFlashLegs, wallyWestComicFlashBoots;
	public static Item wallyWestComicKidFlashHelmet, wallyWestComicKidFlashChestplate, wallyWestComicKidFlashLegs, wallyWestComicKidFlashBoots;
	public static Item rebirthWallyWestHelmet, rebirthWallyWestChestplate, rebirthWallyWestLegs, rebirthWallyWestBoots;
	public static Item comicJesseQuickHelmet, comicJesseQuickChestplate, comicJesseQuickLegs, comicJesseQuickBoots;
	public static Item godspeedHelmet, godspeedChestplate, godspeedLegs, godspeedBoots;
	public static Item quicksilverChestplate, quicksilverLegs, quicksilverBoots;
	public static Item xmenQuicksilverHelmet, xmenQuicksilverChestplate, xmenQuicksilverLegs, xmenQuicksilverBoots;
	public static Item comicQuicksilverChestplate, comicQuicksilverLegs, comicQuicksilverBoots;
	public static Item speedHelmet, speedChestplate, speedLegs, speedBoots;
	public static Item flayChestplate, flayLegs, flayBoots;
	public static Item starLabsTrainingHelmet, starLabsTrainingChestplate, starLabsTrainingLegs, starLabsTrainingBoots;
	
	public static ArrayList<Item> items = new ArrayList<Item>();
	
	public static void preInit() {
		iconItem = new ItemSHIcon();
		
		velocity9 = new ItemVelocity9();
		tachyonCharge = new ItemTachyonCharge();
		tachyonPrototype = new ItemTachyonDevice(TachyonDeviceType.PROTOTYPE);
		tachyonDevice = new ItemTachyonDevice(TachyonDeviceType.DEVICE);
		smallTachyonDevice = new ItemTachyonDevice(TachyonDeviceType.SMALL_DEVICE);
		symbol = new ItemSymbol();
		ring = new ItemRing();
		ringUpgrade = new ItemRingUpgrade();
		philosophersStone = new ItemPhilosophersStone();
		savitarBlade = new ItemSavitarBlade();
		
		flashS1Helmet = new ItemSHSpeedsterArmor(SpeedsterType.flashS1, EntityEquipmentSlot.HEAD);
		flashS1Chestplate = new ItemSHSpeedsterArmor(SpeedsterType.flashS1, EntityEquipmentSlot.CHEST);
		flashS1Legs = new ItemSHSpeedsterArmor(SpeedsterType.flashS1, EntityEquipmentSlot.LEGS);
		flashS1Boots = new ItemSHSpeedsterArmor(SpeedsterType.flashS1, EntityEquipmentSlot.FEET);
		
		flashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.flashS2, EntityEquipmentSlot.HEAD);
		flashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.flashS2, EntityEquipmentSlot.CHEST);
		flashLegs = new ItemSHSpeedsterArmor(SpeedsterType.flashS2, EntityEquipmentSlot.LEGS);
		flashBoots = new ItemSHSpeedsterArmor(SpeedsterType.flashS2, EntityEquipmentSlot.FEET);
		
		futureFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.futureFlash, EntityEquipmentSlot.HEAD);
		futureFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.futureFlash, EntityEquipmentSlot.CHEST);
		futureFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.futureFlash, EntityEquipmentSlot.LEGS);
		futureFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.futureFlash, EntityEquipmentSlot.FEET);
		
		reverseFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.reverseFlash, EntityEquipmentSlot.HEAD);
		reverseFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.reverseFlash, EntityEquipmentSlot.CHEST);
		reverseFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.reverseFlash, EntityEquipmentSlot.LEGS);
		reverseFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.reverseFlash, EntityEquipmentSlot.FEET);
		
		zoomHelmet = new ItemSHSpeedsterArmor(SpeedsterType.zoom, EntityEquipmentSlot.HEAD);
		zoomChestplate = new ItemSHSpeedsterArmor(SpeedsterType.zoom, EntityEquipmentSlot.CHEST);
		zoomLegs = new ItemSHSpeedsterArmor(SpeedsterType.zoom, EntityEquipmentSlot.LEGS);
		zoomBoots = new ItemSHSpeedsterArmor(SpeedsterType.zoom, EntityEquipmentSlot.FEET);
		
		jayGarrickHelmet = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick, EntityEquipmentSlot.HEAD);
		jayGarrickChestplate = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick, EntityEquipmentSlot.CHEST);
		jayGarrickLegs = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick, EntityEquipmentSlot.LEGS);
		jayGarrickBoots = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick, EntityEquipmentSlot.FEET);
		
		jayGarrick2Chestplate = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick2, EntityEquipmentSlot.CHEST);
		jayGarrick2Legs = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick2, EntityEquipmentSlot.LEGS);
		jayGarrick2Boots = new ItemSHSpeedsterArmor(SpeedsterType.jayGarrick2, EntityEquipmentSlot.FEET);
		
		kidFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.kidFlash, EntityEquipmentSlot.HEAD);
		kidFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.kidFlash, EntityEquipmentSlot.CHEST);
		kidFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.kidFlash, EntityEquipmentSlot.LEGS);
		kidFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.kidFlash, EntityEquipmentSlot.FEET);
		
		theRivalHelmet = new ItemSHSpeedsterArmor(SpeedsterType.theRival, EntityEquipmentSlot.HEAD);
		theRivalChestplate = new ItemSHSpeedsterArmor(SpeedsterType.theRival, EntityEquipmentSlot.CHEST);
		theRivalLegs = new ItemSHSpeedsterArmor(SpeedsterType.theRival, EntityEquipmentSlot.LEGS);
		theRivalBoots = new ItemSHSpeedsterArmor(SpeedsterType.theRival, EntityEquipmentSlot.FEET);
		
		trajectoryHelmet = new ItemSHSpeedsterArmor(SpeedsterType.trajectory, EntityEquipmentSlot.HEAD);
		trajectoryChestplate = new ItemSHSpeedsterArmor(SpeedsterType.trajectory, EntityEquipmentSlot.CHEST);
		trajectoryLegs = new ItemSHSpeedsterArmor(SpeedsterType.trajectory, EntityEquipmentSlot.LEGS);
		trajectoryBoots = new ItemSHSpeedsterArmor(SpeedsterType.trajectory, EntityEquipmentSlot.FEET);
		
		jesseQuickHelmet = new ItemSHSpeedsterArmor(SpeedsterType.jesseQuick, EntityEquipmentSlot.HEAD);
		jesseQuickChestplate = new ItemSHSpeedsterArmor(SpeedsterType.jesseQuick, EntityEquipmentSlot.CHEST);
		jesseQuickLegs = new ItemSHSpeedsterArmor(SpeedsterType.jesseQuick, EntityEquipmentSlot.LEGS);
		jesseQuickBoots = new ItemSHSpeedsterArmor(SpeedsterType.jesseQuick, EntityEquipmentSlot.FEET);
		
		savitarHelmet = new ItemSHSpeedsterArmor(SpeedsterType.savitar, EntityEquipmentSlot.HEAD);
		savitarChestplate = new ItemSHSpeedsterArmor(SpeedsterType.savitar, EntityEquipmentSlot.CHEST);
		savitarLegs = new ItemSHSpeedsterArmor(SpeedsterType.savitar, EntityEquipmentSlot.LEGS);
		savitarBoots = new ItemSHSpeedsterArmor(SpeedsterType.savitar, EntityEquipmentSlot.FEET);
		
		blackFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.blackFlash, EntityEquipmentSlot.HEAD);
		blackFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.blackFlash, EntityEquipmentSlot.CHEST);
		blackFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.blackFlash, EntityEquipmentSlot.LEGS);
		blackFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.blackFlash, EntityEquipmentSlot.FEET);
		
		acceleratedManHelmet = new ItemSHSpeedsterArmor(SpeedsterType.acceleratedMan, EntityEquipmentSlot.HEAD);
		acceleratedManChestplate = new ItemSHSpeedsterArmor(SpeedsterType.acceleratedMan, EntityEquipmentSlot.CHEST);
		acceleratedManLegs = new ItemSHSpeedsterArmor(SpeedsterType.acceleratedMan, EntityEquipmentSlot.LEGS);
		acceleratedManBoots = new ItemSHSpeedsterArmor(SpeedsterType.acceleratedMan, EntityEquipmentSlot.FEET);
		
		dceuFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.dceuFlash, EntityEquipmentSlot.HEAD);
		dceuFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.dceuFlash, EntityEquipmentSlot.CHEST);
		dceuFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.dceuFlash, EntityEquipmentSlot.LEGS);
		dceuFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.dceuFlash, EntityEquipmentSlot.FEET);
		
		comicFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.comicFlash, EntityEquipmentSlot.HEAD);
		comicFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.comicFlash, EntityEquipmentSlot.CHEST);
		comicFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.comicFlash, EntityEquipmentSlot.LEGS);
		comicFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.comicFlash, EntityEquipmentSlot.FEET);
		
		comicReverseFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.comicReverseFlash, EntityEquipmentSlot.HEAD);
		comicReverseFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.comicReverseFlash, EntityEquipmentSlot.CHEST);
		comicReverseFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.comicReverseFlash, EntityEquipmentSlot.LEGS);
		comicReverseFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.comicReverseFlash, EntityEquipmentSlot.FEET);
		
		professorZoomHelmet = new ItemSHSpeedsterArmor(SpeedsterType.professorZoom, EntityEquipmentSlot.HEAD);
		professorZoomChestplate = new ItemSHSpeedsterArmor(SpeedsterType.professorZoom, EntityEquipmentSlot.CHEST);
		professorZoomLegs = new ItemSHSpeedsterArmor(SpeedsterType.professorZoom, EntityEquipmentSlot.LEGS);
		professorZoomBoots = new ItemSHSpeedsterArmor(SpeedsterType.professorZoom, EntityEquipmentSlot.FEET);
		
		new52FlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.new52Flash, EntityEquipmentSlot.HEAD);
		new52FlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.new52Flash, EntityEquipmentSlot.CHEST);
		new52FlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.new52Flash, EntityEquipmentSlot.LEGS);
		new52FlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.new52Flash, EntityEquipmentSlot.FEET);
		
		wallyWestComicFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicFlash, EntityEquipmentSlot.HEAD);
		wallyWestComicFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicFlash, EntityEquipmentSlot.CHEST);
		wallyWestComicFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicFlash, EntityEquipmentSlot.LEGS);
		wallyWestComicFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicFlash, EntityEquipmentSlot.FEET);
		
		wallyWestComicKidFlashHelmet = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicKidFlash, EntityEquipmentSlot.HEAD);
		wallyWestComicKidFlashChestplate = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicKidFlash, EntityEquipmentSlot.CHEST);
		wallyWestComicKidFlashLegs = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicKidFlash, EntityEquipmentSlot.LEGS);
		wallyWestComicKidFlashBoots = new ItemSHSpeedsterArmor(SpeedsterType.wallyWestComicKidFlash, EntityEquipmentSlot.FEET);
		
		rebirthWallyWestHelmet = new ItemSHSpeedsterArmor(SpeedsterType.rebirthWallyWest, EntityEquipmentSlot.HEAD);
		rebirthWallyWestChestplate = new ItemSHSpeedsterArmor(SpeedsterType.rebirthWallyWest, EntityEquipmentSlot.CHEST);
		rebirthWallyWestLegs = new ItemSHSpeedsterArmor(SpeedsterType.rebirthWallyWest, EntityEquipmentSlot.LEGS);
		rebirthWallyWestBoots = new ItemSHSpeedsterArmor(SpeedsterType.rebirthWallyWest, EntityEquipmentSlot.FEET);
		
		comicJesseQuickHelmet = new ItemSHSpeedsterArmor(SpeedsterType.comicJesseQuick, EntityEquipmentSlot.HEAD);
		comicJesseQuickChestplate = new ItemSHSpeedsterArmor(SpeedsterType.comicJesseQuick, EntityEquipmentSlot.CHEST);
		comicJesseQuickLegs = new ItemSHSpeedsterArmor(SpeedsterType.comicJesseQuick, EntityEquipmentSlot.LEGS);
		comicJesseQuickBoots = new ItemSHSpeedsterArmor(SpeedsterType.comicJesseQuick, EntityEquipmentSlot.FEET);
		
		godspeedHelmet = new ItemSHSpeedsterArmor(SpeedsterType.godspeed, EntityEquipmentSlot.HEAD);
		godspeedChestplate = new ItemSHSpeedsterArmor(SpeedsterType.godspeed, EntityEquipmentSlot.CHEST);
		godspeedLegs = new ItemSHSpeedsterArmor(SpeedsterType.godspeed, EntityEquipmentSlot.LEGS);
		godspeedBoots = new ItemSHSpeedsterArmor(SpeedsterType.godspeed, EntityEquipmentSlot.FEET);
		
		quicksilverChestplate = new ItemSHSpeedsterArmor(SpeedsterType.quicksilver, EntityEquipmentSlot.CHEST);
		quicksilverLegs = new ItemSHSpeedsterArmor(SpeedsterType.quicksilver, EntityEquipmentSlot.LEGS);
		quicksilverBoots = new ItemSHSpeedsterArmor(SpeedsterType.quicksilver, EntityEquipmentSlot.FEET);
		
		xmenQuicksilverHelmet = new ItemSHSpeedsterArmor(SpeedsterType.xmenQuicksilver, EntityEquipmentSlot.HEAD);
		xmenQuicksilverChestplate = new ItemSHSpeedsterArmor(SpeedsterType.xmenQuicksilver, EntityEquipmentSlot.CHEST);
		xmenQuicksilverLegs = new ItemSHSpeedsterArmor(SpeedsterType.xmenQuicksilver, EntityEquipmentSlot.LEGS);
		xmenQuicksilverBoots = new ItemSHSpeedsterArmor(SpeedsterType.xmenQuicksilver, EntityEquipmentSlot.FEET);
		
		comicQuicksilverChestplate = new ItemSHSpeedsterArmor(SpeedsterType.comicQuicksilver, EntityEquipmentSlot.CHEST);
		comicQuicksilverLegs = new ItemSHSpeedsterArmor(SpeedsterType.comicQuicksilver, EntityEquipmentSlot.LEGS);
		comicQuicksilverBoots = new ItemSHSpeedsterArmor(SpeedsterType.comicQuicksilver, EntityEquipmentSlot.FEET);
		
		speedHelmet = new ItemSHSpeedsterArmor(SpeedsterType.speed, EntityEquipmentSlot.HEAD);
		speedChestplate = new ItemSHSpeedsterArmor(SpeedsterType.speed, EntityEquipmentSlot.CHEST);
		speedLegs = new ItemSHSpeedsterArmor(SpeedsterType.speed, EntityEquipmentSlot.LEGS);
		speedBoots = new ItemSHSpeedsterArmor(SpeedsterType.speed, EntityEquipmentSlot.FEET);
		
		flayChestplate = new ItemSHSpeedsterArmor(SpeedsterType.flay, EntityEquipmentSlot.CHEST);
		flayLegs = new ItemSHSpeedsterArmor(SpeedsterType.flay, EntityEquipmentSlot.LEGS);
		flayBoots = new ItemSHSpeedsterArmor(SpeedsterType.flay, EntityEquipmentSlot.FEET);
		
		starLabsTrainingHelmet = new ItemSHSpeedsterArmor(SpeedsterType.starLabsTraining, EntityEquipmentSlot.HEAD);
		starLabsTrainingChestplate = new ItemSHSpeedsterArmor(SpeedsterType.starLabsTraining, EntityEquipmentSlot.CHEST);
		starLabsTrainingLegs = new ItemSHSpeedsterArmor(SpeedsterType.starLabsTraining, EntityEquipmentSlot.LEGS);
		starLabsTrainingBoots = new ItemSHSpeedsterArmor(SpeedsterType.starLabsTraining, EntityEquipmentSlot.FEET);
	}
	
	public static ItemStack getSymbolFromSpeedsterType(SpeedsterType type, int amount) {
		return new ItemStack(symbol, amount, ItemSymbol.list.indexOf(type));
	}
	
}
