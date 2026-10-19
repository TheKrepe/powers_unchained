package lucraft.mods.heroes.speedsterheroes.util;

import java.util.ArrayList;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityLightningThrowing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySpeedforceJump;
import lucraft.mods.heroes.speedsterheroes.entity.EntityBlackFlash;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeWraith;
import lucraft.mods.heroes.speedsterheroes.items.ItemSpeedsterArmor;
import lucraft.mods.heroes.speedsterheroes.items.ItemSymbol;
import lucraft.mods.heroes.speedsterheroes.items.ItemTachyonDevice;
import lucraft.mods.heroes.speedsterheroes.items.SHItems;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.tileentities.TileEntityPhilosophersStoneChest;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.attributes.LCAttributes;
import lucraft.mods.lucraftcore.events.GetArmSwingMultiplierEvent;
import lucraft.mods.lucraftcore.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.events.SuitMakerResultEvent;
import lucraft.mods.lucraftcore.network.MessageSyncPotionEffects;
import lucraft.mods.lucraftcore.network.PacketDispatcher;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class SHCommonEventHandler {

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent e) {
		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.getEntity())) {
			EntityPlayer player = (EntityPlayer) e.getEntityLiving();
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.getEntityLiving(), SpeedforcePlayerHandler.class);
			AbilityPhasing ability = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities(player), AbilityPhasing.class);

			if (ability != null && ability.isUnlocked() && ability.isEnabled()) {
				player.noClip = true;
				player.motionY = 0;
				player.onGround = true;
			}

			// Savitar Size
			if (data != null && data.isInSpeed && SpeedsterHeroesUtil.getSpeedsterType(player) == SpeedsterType.savitar) {
				if (!player.getEntityAttribute(LCAttributes.SIZE).hasModifier(SpeedsterAttributeModifiers.savitarScale))
					player.getEntityAttribute(LCAttributes.SIZE).applyModifier(SpeedsterAttributeModifiers.savitarScale);
			} else {
				if (player.getEntityAttribute(LCAttributes.SIZE).hasModifier(SpeedsterAttributeModifiers.savitarScale))
					player.getEntityAttribute(LCAttributes.SIZE).removeModifier(SpeedsterAttributeModifiers.savitarScale);
			}
		}

		if (e.getEntityLiving().isPotionActive(SpeedsterHeroesProxy.speedShock)) {
			if (e.getEntityLiving().getActivePotionEffect(SpeedsterHeroesProxy.speedShock).getDuration() == 0) {
				e.getEntityLiving().removePotionEffect(SpeedsterHeroesProxy.speedShock);
			}
		}

		// Velocity 9
		if (e.getEntityLiving().isPotionActive(SpeedsterHeroesProxy.velocity9)) {
			if (e.getEntityLiving().getActivePotionEffect(SpeedsterHeroesProxy.velocity9).getDuration() == 1) {
				PotionEffect wither = new PotionEffect(MobEffects.WITHER, 20 * 30, 2);
				wither.setCurativeItems(new ArrayList<ItemStack>());
				e.getEntityLiving().addPotionEffect(wither);
				if(e.getEntityLiving() instanceof EntityPlayer)
					((EntityPlayer)e.getEntityLiving()).addStat(SHAchievements.velocity9Death);
			} else if (e.getEntityLiving().getActivePotionEffect(SpeedsterHeroesProxy.velocity9).getDuration() == 0) {
				e.getEntityLiving().removePotionEffect(SpeedsterHeroesProxy.velocity9);
				PacketDispatcher.sendToAll(new MessageSyncPotionEffects(e.getEntityLiving()));
			} else if (e.getEntity() instanceof EntityPlayer) {
				SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.getEntityLiving(), SpeedforcePlayerHandler.class);

				if (data != null && data.isInSpeed) {
					if (e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(SpeedsterAttributeModifiers.velocity9)) {
						e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SpeedsterAttributeModifiers.velocity9);
						e.getEntityLiving().getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).removeModifier(SpeedsterAttributeModifiers.velocity9StepHeight);
					}
				} else {
					if (!e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(SpeedsterAttributeModifiers.velocity9)) {
						e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(SpeedsterAttributeModifiers.velocity9);
						e.getEntityLiving().getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).applyModifier(SpeedsterAttributeModifiers.velocity9StepHeight);
					}
				}
			}
		} else if (e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).hasModifier(SpeedsterAttributeModifiers.velocity9)) {
			e.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(SpeedsterAttributeModifiers.velocity9);
			e.getEntityLiving().getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).removeModifier(SpeedsterAttributeModifiers.velocity9StepHeight);
		}
	}

	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent event) {
		if (!event.player.world.isRemote && SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(event.player) && SuperpowerHandler.hasSuperpower(event.player, SpeedsterHeroes.speedforce)) {
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(event.player, SpeedforcePlayerHandler.class);
			if (data != null) {
				data.resetSpeed();
				data.isInSpeed = false;
				System.out.println("awd");
				LucraftCoreUtil.sendSuperpowerUpdatePacket(event.player);
			}
		}
	}

	@SubscribeEvent
	public void onUse(PlayerInteractEvent.RightClickBlock e) {
		if (e.getWorld().getTileEntity(e.getPos()) != null && e.getWorld().getTileEntity(e.getPos()) instanceof TileEntityPhilosophersStoneChest) {
			((TileEntityPhilosophersStoneChest) e.getWorld().getTileEntity(e.getPos())).onRightClick(e.getEntityPlayer());
		}
	}

	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent e) {
		EntityLivingBase entity = e.getEntityLiving();
		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.getEntityLiving()) && SuperpowerHandler.hasSuperpower((EntityPlayer) entity, SpeedsterHeroes.speedforce) && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) entity, SpeedforcePlayerHandler.class).isInSpeed) {
			float speedLevel = SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) entity, SpeedforcePlayerHandler.class).speedLevel;
			float x = MathHelper.clamp((float) (entity.motionX * speedLevel), -3F, 3F);
			float y = speedLevel / 30F;
			float z = MathHelper.clamp((float) (entity.motionZ * speedLevel), -3F, 3F);
			entity.addVelocity(x, y, z);
		}
	}

	@SubscribeEvent
	public void onDamage(LivingFallEvent e) {
		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.getEntity()) && SuperpowerHandler.hasSuperpower((EntityPlayer) e.getEntity(), SpeedsterHeroes.speedforce)) {
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.getEntityLiving(), SpeedforcePlayerHandler.class);

			if (data != null && data.isInSpeed) {
				e.setDistance(e.getDistance() / data.speedLevel);
				
				AbilitySpeedforceJump jump = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities((EntityPlayer) e.getEntityLiving()), AbilitySpeedforceJump.class);
				
				if(e.getEntityLiving().isSneaking() && jump != null && jump.isUnlocked() && jump.getCooldown() == 0) {
					jump.action();
				}
			}
		}
	}

	@SubscribeEvent
	public void breakSpeed(PlayerEvent.BreakSpeed e) {
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(e.getEntityPlayer(), SpeedforcePlayerHandler.class);

		if (data != null && data.isInSpeed) {
			e.setNewSpeed(e.getOriginalSpeed() * MathHelper.clamp(data.speedLevel / 4F, 1, 5));
		}
	}

	@SubscribeEvent
	public void onDamage(LivingHurtEvent e) {
		if (e.getSource() instanceof EntityDamageSource && ((EntityDamageSource) e.getSource()).getSourceOfDamage() != null && SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(((EntityDamageSource) e.getSource()).getSourceOfDamage())) {
			EntityPlayer player = (EntityPlayer) ((EntityDamageSource) e.getSource()).getSourceOfDamage();
			SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
			if (player.getHeldItemMainhand().isEmpty() && data != null && data.isInSpeed) {
				float addition = 4 + data.speedLevel / 5F;
				if (data.isMoving)
					addition *= 1.5F;
				e.setAmount(e.getAmount() + addition);
			}
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent e) {
		if (e.getEntityLiving() instanceof EntityTimeRemnant || e.getEntityLiving() instanceof EntityPlayer) {
			if (e.getSource().getSourceOfDamage() != null && e.getSource().getSourceOfDamage() instanceof EntityTimeWraith) {
				((EntityTimeWraith) e.getSource().getSourceOfDamage()).setDespawnTimer(1);
			}

			if (e.getSource().getSourceOfDamage() != null && e.getSource().getSourceOfDamage() instanceof EntityBlackFlash) {
				((EntityBlackFlash) e.getSource().getSourceOfDamage()).setDespawnTimer(1);

				if (e.getEntityLiving() instanceof EntityPlayer && SpeedsterHeroesUtil.getSpeedsterType(e.getEntityLiving()) == SpeedsterType.zoom) {
					e.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(SpeedsterType.blackFlash.getHelmet()));
					e.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(SpeedsterType.blackFlash.getChestplate()));
					e.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(SpeedsterType.blackFlash.getLegs()));
					e.getEntityLiving().setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(SpeedsterType.blackFlash.getBoots()));
					((EntityPlayer)e.getEntityLiving()).addStat(SHAchievements.becomeBlackFlash);
				}
			}
		}
	}

	@SubscribeEvent
	public void armSwing(GetArmSwingMultiplierEvent e) {
		if (e.entity instanceof EntityPlayer && SuperpowerHandler.hasSuperpower((EntityPlayer) e.entity, SpeedsterHeroes.speedforce) && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.entity, SpeedforcePlayerHandler.class) != null && SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) e.entity, SpeedforcePlayerHandler.class).isInSpeed) {
			e.multiplier = 2;
		}
	}

	@SubscribeEvent
	public void onDimensionChange(PlayerChangedDimensionEvent e) {
		if (SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(e.player)) {
			LucraftCoreUtil.sendSuperpowerUpdatePacket(e.player);
		}
	}

	@SubscribeEvent
	public void loot(LootTableLoadEvent e) {
		if (e.getName().toString().toLowerCase().contains("minecraft:chests/")) {
			LootPool pool = e.getTable().getPool("main");
			for (SpeedsterType type : ItemSymbol.list) {
				LootCondition[] chance = { new RandomChance(0.5F) };
				LootFunction[] count = { new SetCount(chance, new RandomValueRange(1.0F, 3.0F)), new SetMetadata(chance, new RandomValueRange(ItemSymbol.list.indexOf(type), ItemSymbol.list.indexOf(type))) };
				LootEntryItem item = new LootEntryItem(SHItems.symbol, 10, 1, count, chance, "symbol_" + type.getUnlocalizedName());
				pool.addEntry(item);
				// LootPool newPool = new LootPool(item, chance, new
				// RandomValueRange(1.0F), new RandomValueRange(0.0F), name)
			}
		}
	}

	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent e) {
		if (e.pickedUp.getEntityItem() != null && e.pickedUp.getEntityItem().getItem() == SHItems.philosophersStone) {
			e.player.addStat(SHAchievements.brahmastra);
		}
	}
	
	@SubscribeEvent
	public void onLeftClick(PlayerEmptyClickEvent.LeftClick e) {
		AbilityLightningThrowing ability = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities(e.getEntityPlayer()), AbilityLightningThrowing.class);
		
		if(ability != null && ability.isUnlocked())
			ability.throwLightning();
	}
	
	@SubscribeEvent
	public void suitMakerResult(SuitMakerResultEvent e) {
		if(e.getItemStack().getItem() instanceof ItemSpeedsterArmor) {
			e.getEntityPlayer().addStat(SHAchievements.suit);
		}
	}

	@SubscribeEvent
	public void anvilUpdateEvent(AnvilUpdateEvent e) {
		if(e.getLeft().getItem() instanceof ItemTachyonDevice && e.getRight().getItem() == SHItems.tachyonCharge) {
			int max = ((ItemTachyonDevice)e.getLeft().getItem()).getTachyonDeviceType().getMaxCharge();
			if(e.getRight().getTagCompound().getInteger(SHNBTTags.progress) > 0 && e.getLeft().getTagCompound().getInteger("Charge") < max) {
				ItemStack newTachyon = e.getLeft().copy();
				int newCharge = MathHelper.clamp(e.getLeft().getTagCompound().getInteger("Charge") + e.getRight().getTagCompound().getInteger(SHNBTTags.progress), 0, max);
				newTachyon.getTagCompound().setInteger("Charge", newCharge);
				e.setOutput(newTachyon);
				e.setCost(5);
			}
		}
	}
	
}