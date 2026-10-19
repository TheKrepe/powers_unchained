package lucraft.mods.heroes.speedsterheroes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBar;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import lucraft.mods.heroes.speedsterheroes.entity.EntitySpeedMirage;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.items.ItemSpeedsterArmor;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.IFakePlayerEntity;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class SpeedsterHeroesUtil {

	public static Random rand = new Random();

	public static final int defaultSpeedLevelWithSpeedforce = 5;
	public static final int extraSpeedLevelVelocity9 = 3;
	public static final int requiredSpeedLevelForWaterWalking = 5;
	public static final int requiredSpeedLevelForSpeedforcePortal = 12;

	public static final int minSpeedsterLevel = 1;
	public static final int maxSpeedsterLevel = 40;
	public static final int maxExtraSpeedLevels = 5;
	public static final int extraSpeedLevelCost = 5;

	public static boolean isMoving(EntityLivingBase entity) {
		return (entity.distanceWalkedModified / 0.6F != entity.prevDistanceWalkedModified / 0.6F);
		// return entity.prevPosX != entity.posX || entity.prevPosY !=
		// entity.posY || entity.prevPosZ != entity.posZ;
	}

	public static boolean isVelocity9Active(EntityLivingBase entity) {
		return entity.isPotionActive(SpeedsterHeroesProxy.velocity9);
	}

	public static int getRemainingVelocity9Duration(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer && isVelocity9Active(entity))
			return entity.getActivePotionEffect(SpeedsterHeroesProxy.velocity9).getDuration();
		else
			return 0;
	}

	public static SpeedsterType getSpeedsterType(EntityLivingBase entity) {
		if (entity instanceof EntityPlayer || entity instanceof EntityTimeRemnant)
			return hasArmorOn(entity) ? getSpeedsterType(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST)) : null;
		else if (entity instanceof EntitySpeedMirage)
			return getSpeedsterType(((EntitySpeedMirage) entity).acquired);
		else if (entity instanceof EntityBlackFlash)
			return SpeedsterType.blackFlash;
//		else if(entity.getClass().toGenericString().contains("EntityCustomNpc")) {
////			IEntity<?> ientity = NpcAPI.Instance().getIEntity(entity);
//			return SpeedsterType.zoom;
//		}
		return null;
	}

	public static SpeedsterType getSpeedsterType(ItemStack stack) {
		if (stack.getItem() != null && stack.getItem() instanceof ItemSpeedsterArmor) {
			return ((ItemSpeedsterArmor) stack.getItem()).getSpeedsterType();
		}

		return null;
	}

	public static int getSpeedLevels(EntityPlayer player) {
		return getSpeedLevelList(player).size();
	}

	public static final String speedLevelNormal = "normal";
	public static final String speedLevelSuit = "suit";
	public static final String speedLevelVelocity9 = "velocity9";
	public static final String speedLevelTachyon = "tachyon";
	public static final String speedLevelExtra = "extra";

	public static List<SpeedLevelBar> getSpeedLevelList(EntityPlayer player) {
		List<SpeedLevelBar> list = new ArrayList<SpeedLevelBar>();
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);

		for (int i = 0; i < defaultSpeedLevelWithSpeedforce; i++)
			list.add(SpeedLevelBar.baseSpeed);

		SpeedsterType type = getSpeedsterType(player);
		if (type != null) {
			for (int i = 0; i < type.getExtraSpeedLevel(type, player); i++)
				list.add(SpeedLevelBar.speedsterTypeLevel);
		}

		for (int i = 0; i < data.extraSpeedLevels; i++) {
			list.add(SpeedLevelBar.extraSpeed);
		}

		if (isVelocity9Active(player)) {
			for (int i = 0; i < extraSpeedLevelVelocity9; i++)
				list.add(SpeedLevelBar.velocity9Speed);
		}

		ItemStack tachyon = ItemStack.EMPTY;
		ItemStack tachyon1 = getTachyonDeviceFromArmor(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
		ItemStack tachyon2 = player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE);
		
		if(!tachyon1.isEmpty())
			tachyon = tachyon1;
		else if(!tachyon2.isEmpty())
			tachyon = tachyon2;
			
		if (!tachyon.isEmpty() && tachyon.hasTagCompound() && tachyon.getTagCompound().getInteger("Charge") > 0) {
			ItemTachyonDevice device = (ItemTachyonDevice) tachyon.getItem();
			for (int i = 0; i < device.getTachyonDeviceType().getSpeedLevels(); i++)
				list.add(device.getTachyonDeviceType().getSpeedLevelBar());
		}

		return list;
	}

	public static boolean hasArmorOn(EntityLivingBase entity) {
		if (!entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemSpeedsterArmor) {
			return ((ItemSpeedsterArmor) entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()).getSpeedsterType().hasArmorOn(entity);
		}

		return false;
	}

	public static boolean hasArmorOn(EntityPlayer player, SpeedsterType type) {
		return hasArmorOn(player) && ((ItemSpeedsterArmor) player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()).getSpeedsterType() == type;
	}

	public static boolean hasHelmetOn(EntityLivingBase entity) {
		return !entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() && entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() instanceof ItemSpeedsterArmor;
	}

	public static int getMetersForLevel(int level) {
		// return (int) (Math.pow(2, level - 1) * 100);
		switch (level) {
		case 1:
			return 0;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		case 10:
			return (level - 1) * 1000;
		case 11:
		case 12:
		case 13:
		case 14:
			return 10000;
		case 15:
			return 15000;
		case 16:
		case 17:
		case 18:
			return 20000;
		case 19:
		case 20:
			return 25000;
		case 21:
			return 50000;
		case 22:
			return 75000;
		case 23:
			return 100000;
		case 24:
			return 125000;
		case 25:
			return 150000;
		case 26:
			return 190000;
		case 27:
			return 20000;
		case 28:
			return 250000;
		case 29:
			return 300000;
		case 30:
		case 31:
		case 32:
		case 33:
		case 34:
		case 35:
			return 350000;
		case 36:
		case 37:
		case 38:
		case 39:
		case 40:
			return 375000;
		default:
			return 0;
		}
	}

	public static int getSpeedsterPointsForLevel(int level) {
		if (level < 30 && (level == 5 || level == 10 || level == 15 || level == 20 || level == 25))
			return 10;
		else if (level > 30 && (level == 35))
			return 15;
		else if (level == 30)
			return 15;
		else if (level == 40)
			return 20;
		else
			return 5;
	}

	public static boolean isEntityAvailableForSpeedforce(Entity entity) {
		return entity != null && entity instanceof EntityPlayer && !(entity instanceof IFakePlayerEntity);
	}

	public static BlockPos getRandomPositionInNear(World world, BlockPos pos, int radius) {
		return getRandomPositionInNear(world, pos, radius, 20);
	}

	public static BlockPos getRandomPositionInNear(World world, BlockPos pos, int radius, int tries) {
		if (tries <= 0)
			return pos;
		BlockPos newPos = new BlockPos(pos.getX() + rand.nextInt(radius) - radius / 2, pos.getY() + rand.nextInt(radius) - radius / 2, pos.getZ() + rand.nextInt(radius) - radius / 2);
		
		while (!world.isBlockFullCube(newPos))
			newPos = newPos.down();

		newPos = newPos.up();

		if (!world.isBlockFullCube(newPos) && !world.isBlockFullCube(newPos.up()) && newPos.getY() >= pos.getY())
			return newPos;
		else
			return getRandomPositionInNear(world, newPos, radius, tries - 1);
	}

	public static int getRecommendedAmountForArmorSlot(int armorSlot) {
		return armorSlot == 0 ? 2 : (armorSlot == 1 ? 4 : (armorSlot == 2 ? 3 : 2));
	}

	public static int getRecommendedAmountForArmorSlotWithAddition(EntityEquipmentSlot armorSlot, SpeedsterType type) {
		float addition = type.addDefaultAbilities(null, new ArrayList<Ability>()).size() / 2F + type.getExtraSpeedLevel(type, null) / 5F;
		return (armorSlot == EntityEquipmentSlot.HEAD ? 1 : (armorSlot == EntityEquipmentSlot.CHEST ? 3 : (armorSlot == EntityEquipmentSlot.LEGS ? 2 : 1))) + Math.round(addition);
	}

	public static List<ItemStack> getOresWithAmount(String ores, int amount) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();

		for (ItemStack stacks : OreDictionary.getOres(ores)) {
			list.add(new ItemStack(stacks.getItem(), amount, stacks.getMetadata()));
		}

		return list;
	}

	public static boolean hasTachyonDevice(ItemStack stack) {
		return LucraftCoreUtil.hasArmorThisUpgrade(stack, SHItems.tachyonDevice) || LucraftCoreUtil.hasArmorThisUpgrade(stack, SHItems.tachyonPrototype) || LucraftCoreUtil.hasArmorThisUpgrade(stack, SHItems.smallTachyonDevice);
	}
	
	public static boolean isTachyonDevice(ItemStack stack) {
		return !stack.isEmpty() && (stack.getItem() == SHItems.tachyonDevice || stack.getItem() == SHItems.tachyonPrototype || stack.getItem() == SHItems.smallTachyonDevice);
	}
	
	public static ItemStack getTachyonDeviceFromArmor(ItemStack armor) {
		for(ItemStack s : LucraftCoreUtil.getArmorUpgrades(armor)) {
			if(!s.isEmpty() && s.getItem() instanceof ItemTachyonDevice) {
				return s;
			}
		}
		return ItemStack.EMPTY;
	}
	
}
