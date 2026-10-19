package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityGrabbedBlock;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageSendInfoToServer;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.particles.ParticleColoredCloud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class AbilityGrabEntity extends AbilityHeld {

    public Entity grabbed;

    public AbilityGrabEntity(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        mc.getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        mc.getRenderItem().renderItemIntoGUI(new ItemStack(HEItems.SPACE_STONE), 0, 0);
        GlStateManager.popMatrix();
        mc.getRenderItem().zLevel = zLevel;
    }

    @Override
    public void updateTick() {
        if (grabbed == null) {
            RayTraceResult rtr = getPosLookingAt();

            if (rtr.typeOfHit == RayTraceResult.Type.ENTITY)
                this.grabbed = rtr.entityHit;
            else if (rtr.typeOfHit == RayTraceResult.Type.BLOCK) {

                if (this.entity.world.isRemote || !LCEntityHelper.canDestroyBlock(this.entity.world, rtr.getBlockPos(), this.entity))
                    return;

                EntityGrabbedBlock entity = new EntityGrabbedBlock(this.entity.world, rtr.getBlockPos());
                entity.world.spawnEntity(entity);
                this.grabbed = entity;
            }
        }

        if (grabbed != null) {

            if(!grabbed.isEntityAlive()) {
                this.grabbed = null;
                this.setEnabled(false);
                return;
            }

            Vec3d pos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(entity.getLookVec().scale(entity instanceof EntityPlayer ? entity.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() * 1.2D : 3D));
            grabbed.motionX = 0;
            grabbed.motionY = 0;
            grabbed.motionZ = 0;
            grabbed.setPositionAndRotation(pos.x, pos.y, pos.z, grabbed.rotationYaw, grabbed.rotationPitch);
            grabbed.setPositionAndUpdate(pos.x, pos.y, pos.z);
            grabbed.fallDistance = entity.fallDistance;

            if (entity.world instanceof WorldServer) {
                for (EntityPlayer players : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(grabbed)) {
                    if (players instanceof EntityPlayerMP)
                        ((EntityPlayerMP) players).connection.sendPacket(new SPacketEntityTeleport(grabbed));
                }
                if (grabbed instanceof EntityPlayerMP)
                    ((EntityPlayerMP) grabbed).connection.sendPacket(new SPacketEntityTeleport(grabbed));
            }

            if (entity.ticksExisted % 2 == 0) {
                Random rand = new Random();
                Vec3d endPos = grabbed.getPositionVector().add(-grabbed.width / 2D, 0, -grabbed.width / 2D).add(rand.nextFloat() * grabbed.width, rand.nextFloat() * grabbed.height, rand.nextFloat() * grabbed.width);
                Vec3d startPos = entity.getPositionVector().add(0, entity.height / 2D, 0);
                double d = 5D;
                double brightness = rand.nextFloat();
                PlayerHelper.spawnParticleForAll(entity.world, 50, ParticleColoredCloud.ID, startPos.x, startPos.y, startPos.z, (endPos.x - startPos.x) / d, (endPos.y - startPos.y) / d, (endPos.z - startPos.z) / d, (int) (brightness * 176), 134 + (int) (brightness * 96), 198 + (int) (brightness * 57));
            }
        }
    }

    @Override
    public void onKeyPressed() {
        super.onKeyPressed();
        this.grabbed = null;
    }

    @Override
    public void onKeyReleased() {
        super.onKeyReleased();
        this.grabbed = null;
    }

    public RayTraceResult getPosLookingAt() {
        Vec3d lookVec = entity.getLookVec();
        double distance = entity instanceof EntityPlayer ? entity.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue() : 5D;
        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d pos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(scale));

            if (entity.world.isBlockFullCube(new BlockPos(pos)) && !entity.world.isAirBlock(new BlockPos(pos))) {
                return new RayTraceResult(RayTraceResult.Type.BLOCK, pos, null, new BlockPos(pos));
            } else {
                Vec3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vec3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : this.entity.world.getEntitiesWithinAABBExcludingEntity(this.entity, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(entity.getPositionVector().add(lookVec.scale(distance)), null);
    }

    public void throwEntity() {
        if (this.grabbed != null) {

            if(grabbed instanceof EntityGrabbedBlock)
                ((EntityGrabbedBlock) grabbed).released = true;
            if (grabbed instanceof EntityThrowable) {
                ((EntityThrowable) grabbed).shoot(this.entity, this.entity.rotationPitch, this.entity.rotationYaw, 0, 1, 1.5F);
            } else {
                Vec3d lookVec = this.entity.getLookVec().scale(3);
                grabbed.motionX = lookVec.x;
                grabbed.motionY = lookVec.y;
                grabbed.motionZ = lookVec.z;
            }

            if (entity.world instanceof WorldServer && !grabbed.isDead) {
                for (EntityPlayer players : ((WorldServer) entity.world).getEntityTracker().getTrackingPlayers(grabbed)) {
                    if (players instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) players).connection.sendPacket(new SPacketEntityTeleport(grabbed));
                        ((EntityPlayerMP) players).connection.sendPacket(new SPacketEntityVelocity(grabbed));
                    }
                }
                if (grabbed instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) grabbed).connection.sendPacket(new SPacketEntityTeleport(grabbed));
                    ((EntityPlayerMP) grabbed).connection.sendPacket(new SPacketEntityVelocity(grabbed));
                }
            }

            this.grabbed = null;
            this.setEnabled(false);
        }
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class EventHandler {

        @SubscribeEvent
        public static void onTick(TickEvent.PlayerTickEvent e) {
            if (e.player == Minecraft.getMinecraft().player && Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown()) {
                if (Ability.hasAbility(e.player, AbilityGrabEntity.class))
                    HEPacketDispatcher.sendToServer(new MessageSendInfoToServer(MessageSendInfoToServer.ServerMessageType.THROW_GRABBED_ENTITY));
            }
        }

    }

}
