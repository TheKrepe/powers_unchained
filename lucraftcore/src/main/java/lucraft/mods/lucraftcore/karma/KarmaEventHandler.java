package lucraft.mods.lucraftcore.karma;

import lucraft.mods.lucraftcore.karma.events.EntityKnockOutEvent;
import lucraft.mods.lucraftcore.karma.potions.PotionKnockOut;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.network.MessageSyncPotionEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class KarmaEventHandler {

    @SubscribeEvent
    public void onJump(LivingJumpEvent e) {
        if (e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            e.getEntityLiving().motionX = 0;
            e.getEntityLiving().motionY = 0;
            e.getEntityLiving().motionZ = 0;
        }
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent e) {
        if (e.getTarget() != null && e.getTarget().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            e.getEntityLiving().setRevengeTarget(null);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent e) {
        if (e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            e.getEntityLiving().rotationYaw = e.getEntityLiving().prevRotationYaw;
            e.getEntityLiving().rotationPitch = e.getEntityLiving().prevRotationPitch;
            e.getEntityLiving().rotationYawHead = 0;
            e.getEntityLiving().fallDistance = 0;

            if (e.getEntityLiving().getActivePotionEffect(PotionKnockOut.POTION_KNOCK_OUT).getDuration() == 1) {
                MinecraftForge.EVENT_BUS.post(new EntityKnockOutEvent.WakeUp(e.getEntityLiving()));
                e.getEntityLiving().removePotionEffect(PotionKnockOut.POTION_KNOCK_OUT);

                if (e.getEntityLiving() instanceof EntityPlayer) {
                    BlockPos respawn = ((EntityPlayer) e.getEntityLiving()).getBedLocation() != null ? ((EntityPlayer) e.getEntityLiving()).getBedLocation() : ((EntityPlayer) e.getEntityLiving()).world.getSpawnPoint();
                    e.getEntityLiving().setPositionAndUpdate(respawn.getX(), respawn.getY(), respawn.getZ());
                } else
                    e.getEntityLiving().setHealth(0);

                LCPacketDispatcher.sendToAll(new MessageSyncPotionEffects(e.getEntityLiving()));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent e) {
        if (e.getSource() != null && e.getSource().getTrueSource() != null && e.getSource().getTrueSource() instanceof EntityPlayer && !e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            EntityPlayer player = (EntityPlayer) e.getSource().getTrueSource();

            boolean save = false;
            for (EntityLivingBase entity : e.getEntity().getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(e.getEntity().getPosition().add(-10, -10, -10), e.getEntity().getPosition().add(10, 10, 10)))) {
                if (!entity.isDead && e.getEntityLiving() == entity.getRevengeTarget()) {
                    KarmaHandler.increaseKarmaStat(player, KarmaStat.SAVES);
                    save = true;
                }
            }

            if (!save) {
                if (KarmaHandler.isMonster(e.getEntityLiving())) {
                    KarmaHandler.increaseKarmaStat(player, KarmaStat.MONSTER_KILL);
                } else if (KarmaHandler.isGoodEntity(e.getEntityLiving())) {
                    KarmaHandler.increaseKarmaStat(player, KarmaStat.GOOD_PLAYER_KILL);
                } else if (e.getEntityLiving() instanceof EntityPlayer && KarmaHandler.isEvilPlayer((EntityPlayer) e.getEntityLiving())) {
                    KarmaHandler.increaseKarmaStat(player, KarmaStat.BAD_PLAYER_KILL);
                }
            }
        }
    }

    @SubscribeEvent
    public void onTaming(AnimalTameEvent e) {
        KarmaHandler.increaseKarmaStat(e.getTamer(), KarmaStat.TAMING);
    }

    @SubscribeEvent
    public void onLivingHurtEvent(LivingHurtEvent e) {
        if (e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT) && e.getSource().getImmediateSource() != null && !(e.getSource().getImmediateSource() instanceof EntityPlayer)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingAttackEvent(LivingAttackEvent e) {
        if (e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT) && e.getSource().getImmediateSource() != null && !(e.getSource().getImmediateSource() instanceof EntityPlayer)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightclick(PlayerInteractEvent.EntityInteract e) {
        if (e.getTarget() instanceof EntityVillager && ((EntityVillager) e.getTarget()).isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            EntityVillager villager = (EntityVillager) e.getTarget();
            e.setCanceled(true);

            MerchantRecipeList recipeList = villager.getRecipes(e.getEntityPlayer());
            boolean hasStolen = false;

            if (recipeList != null) {
                for (MerchantRecipe aRecipeList : recipeList) {
                    PlayerHelper.givePlayerItemStack(e.getEntityPlayer(), aRecipeList.getItemToSell().copy());
                    hasStolen = true;
                }

                recipeList.clear();

                if (hasStolen)
                    KarmaHandler.increaseKarmaStat(e.getEntityPlayer(), KarmaStat.THEFTS);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void setupModel(RenderModelEvent e) {
        if (e.getEntityLiving().isPotionActive(PotionKnockOut.POTION_KNOCK_OUT)) {
            GlStateManager.translate(0, -e.getEntityLiving().width / 3F, -e.getEntityLiving().height / 2F);
            GlStateManager.rotate(-90, 1, 0, 0);
            GlStateManager.rotate(-90, 0, 1, 0);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderView(EntityViewRenderEvent.CameraSetup e) {
        if (e.getEntity() == Minecraft.getMinecraft().player) {
            if (Minecraft.getMinecraft().player.isPotionActive(PotionKnockOut.POTION_KNOCK_OUT) && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                GlStateManager.translate(0, Minecraft.getMinecraft().player.getEyeHeight() * 0.8F, 0);
            }
        }
    }

}
