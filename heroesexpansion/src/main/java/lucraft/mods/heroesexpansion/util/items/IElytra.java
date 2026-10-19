package lucraft.mods.heroesexpansion.util.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.ItemSuitSetElytra;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageElytraFlying;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public interface IElytra {

    boolean canUseElytra(EntityPlayer player, ItemStack stack);

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        // Credit: UpcraftLP

        private static final Map<EntityPlayerMP, Boolean> FLYING = new WeakHashMap<>();
        private static boolean lastTickJump = false;

        public static synchronized void setFlying(EntityPlayerMP playerMP, boolean flying) {
            FLYING.put(playerMP, flying);
            if (flying) {
                if (!playerMP.isElytraFlying())
                    playerMP.setElytraFlying();
            } else if (playerMP.isElytraFlying())
                playerMP.clearElytraFlying();
        }

        public static boolean isFlying(EntityPlayerMP playerMP) {
            return FLYING.getOrDefault(playerMP, false);
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onPressKey(InputUpdateEvent event) {
            EntityPlayer player = event.getEntityPlayer();

            if (!lastTickJump && event.getMovementInput().jump && !player.isElytraFlying() && !player.onGround && !player.isInWater() && !player.isRiding() && !player.capabilities.isFlying) {
                Minecraft mc = Minecraft.getMinecraft();
                ItemStack stack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (stack.getItem() instanceof IElytra && ((IElytra) stack.getItem()).canUseElytra(mc.player, stack)) {
                    HEPacketDispatcher.sendToServer(new MessageElytraFlying());
                }
            }
            lastTickJump = event.getMovementInput().jump;
        }

        @SubscribeEvent
        public static void onPlayerUpdate(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                if (event.player instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) event.player;
                    ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                    boolean b = false;
                    if (stack.getItem() instanceof IElytra) {
                        IElytra elytra = (IElytra) stack.getItem();
                        b = elytra.canUseElytra(player, stack) && !player.onGround && !player.isRiding() && !player.capabilities.isFlying && !player.isInWater();
                    }
                    if (isFlying(player)) {
                        player.fallDistance = 0.0F;
                        setFlying(player, b);
                    }
                }
            }

            if (event.player.isElytraFlying() && SuitSet.hasSuitSetOn(event.player) && event.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemSuitSetElytra) {
                if (event.player.moveForward > 0F && event.player.ticksExisted % 2 == 0) {
                    Vec3d vec3d = event.player.getLookVec();
                    double d0 = 1.5D;
                    double d1 = 0.1D;
                    event.player.motionX += vec3d.x * d1 + (vec3d.x * d0 - event.player.motionX) * 0.5D;
                    event.player.motionY += vec3d.y * d1 + (vec3d.y * d0 - event.player.motionY) * 0.5D;
                    event.player.motionZ += vec3d.z * d1 + (vec3d.z * d0 - event.player.motionZ) * 0.5D;
                }

                SuitSet suitSet = SuitSet.getSuitSet(event.player);

                if (suitSet != null && suitSet.getData() != null && suitSet.getData().getBoolean("elytra_particles")) {
                    Random rand = new Random();
                    Vec3d left = new Vec3d(-0.28D, event.player.height / 2D, -0.28D);
                    left = left.rotateYaw(-event.player.renderYawOffset).add(-event.player.motionX * 0.2D, -event.player.motionY * 0.2D, -event.player.motionZ * 0.2D);

                    Vec3d right = new Vec3d(0.28D, event.player.height / 2D, -0.28D);
                    right = right.rotateYaw(-event.player.renderYawOffset).add(-event.player.motionX * 0.2D, -event.player.motionY * 0.2D, -event.player.motionZ * 0.2D);

                    event.player.world.spawnParticle(rand.nextBoolean() ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.CLOUD, event.player.posX + left.x, event.player.posY + left.y, event.player.posZ + left.z, rand.nextDouble() * 0.05D - 0.025D, -0.2D, rand.nextDouble() * 0.05D - 0.025D);
                    event.player.world.spawnParticle(rand.nextBoolean() ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.CLOUD, event.player.posX + right.x, event.player.posY + right.y, event.player.posZ + right.z, rand.nextDouble() * 0.05D - 0.025D, -0.2D, rand.nextDouble() * 0.05D - 0.025D);

                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModel(RenderModelEvent.SetRotationAngels e) {
            if (e.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) e.getEntity()).isElytraFlying() && SuitSet.hasSuitSetOn((EntityLivingBase) e.getEntity()) && ((EntityLivingBase) e.getEntity()).getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemSuitSetElytra) {
                e.setCanceled(true);
                Vec3d motion = e.getEntity() == Minecraft.getMinecraft().player ? new Vec3d(e.getEntity().motionX, e.getEntity().motionY, e.getEntity().motionZ) : new Vec3d(e.getEntity().posX - e.getEntity().prevPosX, e.getEntity().posY - e.getEntity().prevPosY, e.getEntity().posZ - e.getEntity().prevPosZ);
                motion = motion.normalize();
                e.model.bipedRightArm.rotateAngleZ = (float) Math.toRadians(motion.y < 0F ? (1F + motion.y) * 90F : 90F);
                e.model.bipedLeftArm.rotateAngleZ = (float) Math.toRadians(motion.y < 0F ? (1F + motion.y) * -90F : -90F);
                if (e.model instanceof ModelPlayer) {
                    ((ModelPlayer) e.model).bipedRightArmwear.rotateAngleZ = e.model.bipedRightArm.rotateAngleZ;
                    ((ModelPlayer) e.model).bipedLeftArmwear.rotateAngleZ = e.model.bipedLeftArm.rotateAngleZ;
                }
            }
        }
    }

}
