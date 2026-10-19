package lucraft.mods.heroes.speedsterheroes.superpower;

import java.util.ArrayList;
import java.util.List;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityAccelerate;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityDecelerate;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySlowMotion;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySuperSpeed;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityWallRunning;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityWaterRunning;
import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBar;
import lucraft.mods.heroes.speedsterheroes.client.render.speedlevelbars.SpeedLevelBarTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonCharge;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailTypeCustom;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterAttributeModifiers;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.attributes.LCAttributes;
import lucraft.mods.lucraftcore.extendedinventory.ExtendedPlayerInventory;
import lucraft.mods.lucraftcore.items.InventoryArmorUpgrade;
import lucraft.mods.lucraftcore.superpower.SuperpowerPlayerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class SpeedforcePlayerHandler extends SuperpowerPlayerHandler {

	public SpeedforcePlayerHandler(EntityPlayer player) {
		super(player, SpeedsterHeroes.speedforce);
	}

	// --- Variables
	// -------------------------------------------------------------

	public boolean isInSpeed;
	public int speedLevel;
	public int extraSpeedLevels;
	public boolean isWallRunning;
	public TrailTypeCustom customTrailType;
	public List<TeleportDestination> waypoints = new ArrayList<TeleportDestination>();
	public int chosenWaypointIndex = -1;

	public boolean isMoving = false;

	// ---------------------------------------------------------------------------

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound compound = super.writeToNBT(nbt);
		compound.setBoolean("isInSpeed", isInSpeed);
		compound.setInteger("speedLevel", speedLevel);
		compound.setInteger("extraSpeedLevels", extraSpeedLevels);
		compound.setBoolean("isWallRunning", isWallRunning);
		compound.setInteger("ChosenWaypointIndex", chosenWaypointIndex);

		NBTTagCompound trailNBT = new NBTTagCompound();
		compound.setTag("customTrailType", trailNBT);

		NBTTagList tagList = new NBTTagList();
		for (TeleportDestination teleports : waypoints) {
			tagList.appendTag(teleports.serializeNBT());
		}
		compound.setTag("Waypoints", tagList);

		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		isInSpeed = compound.getBoolean("isInSpeed");
		speedLevel = compound.getInteger("speedLevel");
		extraSpeedLevels = compound.getInteger("extraSpeedLevels");
		isWallRunning = compound.getBoolean("isWallRunning");
		chosenWaypointIndex = compound.getInteger("ChosenWaypointIndex");
		this.customTrailType = new TrailTypeCustom(getStyleNBTTag(), player);

		this.waypoints = new ArrayList<TeleportDestination>();
		NBTTagList tagList = compound.getTagList("Waypoints", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			TeleportDestination teleports = new TeleportDestination("", BlockPos.ORIGIN, 0);
			teleports.deserializeNBT(tagList.getCompoundTagAt(i));
			this.waypoints.add(teleports);
		}
	}

	@Override
	public void onApplyPower() {
		player.addStat(SHAchievements.becomeSpeedster);
	}

	@Override
	public void onRemovePower() {
		resetSpeed();
	}

	@Override
	public void setStyleNBTTag(NBTTagCompound tag) {
		super.setStyleNBTTag(tag);
		this.customTrailType = new TrailTypeCustom(getStyleNBTTag(), player);
	}

	@Override
	public void onUpdate(Phase phase) {
		super.onUpdate(phase);

		if (phase == Phase.END) {
			return;
		}

		if (isInSpeed) {
			SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(player);
			boolean isMoving = SpeedsterHeroesUtil.isMoving(player);
			this.isMoving = isMoving;
			boolean isSprinting = player.isSprinting();
			float walkedDifference = (player.distanceWalkedModified / 0.6F) - (player.prevDistanceWalkedModified / 0.6F);
			addXP((int) (type == SpeedsterType.starLabsTraining ? 2 * walkedDifference : walkedDifference), false);

			// Tachyon Charge
			if (isMoving) {
				if (!player.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && player.getHeldItem(EnumHand.MAIN_HAND).getItem() == SHItems.tachyonCharge) {
					ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
					NBTTagCompound nbt = stack.getTagCompound();
					int progress = nbt.getInteger(SHNBTTags.progress);
					progress += walkedDifference;
					nbt.setInteger(SHNBTTags.progress, progress > ItemTachyonCharge.maxProgress ? ItemTachyonCharge.maxProgress : progress);
					stack.setTagCompound(nbt);
				}

				if (!player.getHeldItem(EnumHand.OFF_HAND).isEmpty() && player.getHeldItem(EnumHand.OFF_HAND).getItem() == SHItems.tachyonCharge) {
					ItemStack stack = player.getHeldItem(EnumHand.OFF_HAND);
					NBTTagCompound nbt = stack.getTagCompound();
					int progress = nbt.getInteger(SHNBTTags.progress);
					progress += walkedDifference;
					nbt.setInteger(SHNBTTags.progress, progress > ItemTachyonCharge.maxProgress ? ItemTachyonCharge.maxProgress : progress);
					stack.setTagCompound(nbt);
				}
			}

			// Wall Running
			if (Ability.getAbilityFromClass(getAbilities(), AbilityWallRunning.class).isUnlocked()) {
				if (player.isCollidedHorizontally && !player.isCollidedVertically) {
					player.motionY = MathHelper.clamp(speedLevel / 6F, 0.5F, 2F);
					this.isWallRunning = true;
				} else {
					if (isWallRunning) {
						this.isWallRunning = false;
						player.motionY = 0.2F;
					}
				}
			}

			// Water Running
			if (isMoving && Ability.getAbilityFromClass(getAbilities(), AbilityWaterRunning.class).isUnlocked() && speedLevel >= SpeedsterHeroesUtil.requiredSpeedLevelForWaterWalking && player.world.getBlockState(player.getPosition().add(0, -1, 0)).getBlock() instanceof BlockLiquid && isSprinting) {
				player.posY -= player.motionY;
				player.motionY = 0D;
				player.fallDistance = 0.0F;
				player.onGround = true;
			}

			// Friction Burn
			if (SHConfig.frictionBurn && player.onGround && speedLevel > 3 && walkedDifference > 1.6F && type == null && !player.capabilities.isCreativeMode) {
				player.setFire(10);
				player.addStat(SHAchievements.frictionBurn);
			}

			// Speed Level Check
			List<SpeedLevelBar> speedLevels = SpeedsterHeroesUtil.getSpeedLevelList(player);
			int maxSpeedLevels = speedLevels.size();
			if (speedLevel > maxSpeedLevels) {
				setSpeedLevel(maxSpeedLevels);
				LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
			}
			
			if (isMoving && speedLevels.get(this.speedLevel - 1) instanceof SpeedLevelBarTachyonDevice) {
				if (SpeedsterHeroesUtil.hasTachyonDevice(player.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
					ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
					ItemStack tachyon = SpeedsterHeroesUtil.getTachyonDeviceFromArmor(chest);
					NBTTagCompound nbt = tachyon.getTagCompound();
					if (nbt.getInteger("Charge") > 0) {
						nbt.setInteger("Charge", nbt.getInteger("Charge") - 1);
						tachyon.setTagCompound(nbt);
						InventoryArmorUpgrade inv = new InventoryArmorUpgrade(chest);
						int slot = 0;

						for (int i = 0; i < inv.getSizeInventory(); i++) {
							inv.getStackInSlot(i);
							if (!inv.getStackInSlot(i).isEmpty() && ItemStack.areItemsEqual(chest, inv.getStackInSlot(i))) {
								slot = i;
								break;
							}
						}

//						inv.setInventorySlotContents(slot, chest);
					}
				} else if (SpeedsterHeroesUtil.isTachyonDevice(player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE))) {
					ItemStack tachyon = player.getCapability(LucraftCore.EXTENDED_INVENTORY, null).getInventory().getStackInSlot(ExtendedPlayerInventory.SLOT_MANTLE);
					NBTTagCompound nbt = tachyon.getTagCompound();
					if (nbt.getInteger("Charge") > 0) {
						nbt.setInteger("Charge", nbt.getInteger("Charge") - 1);
						tachyon.setTagCompound(nbt);
					}
				}
			}

			// -----------------------------------------------------------------------
		} else {
			if (isWallRunning)
				isWallRunning = false;
		}
	}

	@Override
	public Ability getAbilityForKey(LucraftKeys key) {
		if (key == LucraftKeys.SUPERPOWER_2)
			return Ability.getAbilityFromClass(getAbilities(), AbilityAccelerate.class);
		if (key == LucraftKeys.SUPERPOWER_3)
			return Ability.getAbilityFromClass(getAbilities(), AbilityDecelerate.class);
		if (key == LucraftKeys.SUPERPOWER_4)
			return Ability.getAbilityFromClass(getAbilities(), AbilitySuperSpeed.class);
		if (key == LucraftKeys.SUPERPOWER_5)
			return Ability.getAbilityFromClass(getAbilities(), AbilitySlowMotion.class);
		return super.getAbilityForKey(key);
	}

	@Override
	public void onWorldJoin() {
		resetSpeed();
		LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
	}

	@Override
	public void onRespawn() {
		resetSpeed();
		LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
	}

	@Override
	public void onLevelUp(int newLevel) {
		if (newLevel == 10 || newLevel == 15 || newLevel == 20 || newLevel == 25 || newLevel == 30) {
			if (extraSpeedLevels < 5) {
				extraSpeedLevels++;
				LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
			}
		}
	}

	// ---------------------------------------------------------------------------

	public void setSpeedLevel(int i) {
		this.speedLevel = i;
		if (i <= 0)
			speedLevel = 1;

		resetSpeed();
		player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(i));
		player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).applyModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(i));
		int stepheight = (int) MathHelper.clamp(speedLevel, 0, 5F);
		player.getEntityAttribute(LCAttributes.STEP_HEIGHT).applyModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(stepheight));
		this.isInSpeed = true;
		LucraftCoreUtil.sendSuperpowerUpdatePacket(player);
	}

	public void increaseDecreaseSpeedLevel(boolean faster) {
		if (isInSpeed) {
			int newPoints = speedLevel + (faster ? 1 : -1);
			if (newPoints > SpeedsterHeroesUtil.getSpeedLevels(player) || newPoints < 1)
				return;
			setSpeedLevel(newPoints);
		}
	}

	public void toggleSpeed() {
		if (isInSpeed) {
			resetSpeed();
		} else {
			setSpeedLevel(1);
		}
		LucraftCoreUtil.sendSuperpowerUpdatePacketToAllPlayers(player);
	}

	public void resetSpeed() {
		for (int i = 0; i < SpeedsterAttributeModifiers.amountOfAttributes; i++) {
			player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(i + 1));
			player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).removeModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(i + 1));
			player.getEntityAttribute(LCAttributes.STEP_HEIGHT).removeModifier(SpeedsterAttributeModifiers.getSpeedsterAttributeModifier(i + 1));
		}
		this.isInSpeed = false;
	}

}
