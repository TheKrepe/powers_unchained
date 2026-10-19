package lucraft.mods.lucraftcore.util.helper;

import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.network.MessageSpawnParticle;
import lucraft.mods.lucraftcore.util.network.MessageSwingArm;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerHelper {

    public static void playSound(World world, EntityPlayer player, double x, double y, double z, SoundEvent sound, SoundCategory category) {
        playSound(world, player, x, y, z, sound, category, 1F, 1F);
    }

    public static void playSound(World world, EntityPlayer player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketCustomSound(sound.getRegistryName().toString(), category, x, y, z, volume, pitch));
        }
    }

    public static void playSoundToAll(World world, double x, double y, double z, double range, SoundEvent sound, SoundCategory category) {
        playSoundToAll(world, x, y, z, range, sound, category, 1, 1);
    }

    public static void playSoundToAll(World world, double x, double y, double z, double range, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        AxisAlignedBB a = new AxisAlignedBB(new BlockPos(x - range, y - range, z - range), new BlockPos(x + range, y + range, z + range));
        for (EntityPlayer players : world.getEntitiesWithinAABB(EntityPlayer.class, a)) {
            if (players instanceof EntityPlayerMP) {
                ((EntityPlayerMP) players).connection.sendPacket(new SPacketCustomSound(sound.getRegistryName().toString(), category, x, y, z, volume, pitch));
            }
        }
    }

    public static void spawnParticle(EntityPlayer player, int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        if (player instanceof EntityPlayerMP) {
            LCPacketDispatcher.sendTo(new MessageSpawnParticle(particleId, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters), (EntityPlayerMP) player);
        }
    }

    public static void spawnParticleForAll(World world, double range, int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        AxisAlignedBB a = new AxisAlignedBB(new BlockPos(xCoord - range, yCoord - range, zCoord - range), new BlockPos(xCoord + range, yCoord + range, zCoord + range));
        for (EntityPlayer players : world.getEntitiesWithinAABB(EntityPlayer.class, a)) {
            spawnParticle(players, particleId, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
        }
    }

    public static void spawnParticle(EntityPlayer player, EnumParticleTypes particleIn, boolean longDistanceIn, float xIn, float yIn, float zIn, float xOffsetIn, float yOffsetIn, float zOffsetIn, float speedIn, int countIn, int... argumentsIn) {
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketParticles(particleIn, longDistanceIn, xIn, yIn, zIn, xOffsetIn, yOffsetIn, zOffsetIn, speedIn, countIn, argumentsIn));
        }
    }

    public static void spawnParticleForAll(World world, double range, EnumParticleTypes particleIn, boolean longDistanceIn, float xIn, float yIn, float zIn, float xOffsetIn, float yOffsetIn, float zOffsetIn, float speedIn, int countIn, int... argumentsIn) {
        AxisAlignedBB a = new AxisAlignedBB(new BlockPos(xIn - range, yIn - range, zIn - range), new BlockPos(xIn + range, yIn + range, zIn + range));
        for (EntityPlayer players : world.getEntitiesWithinAABB(EntityPlayer.class, a)) {
            spawnParticle(players, particleIn, longDistanceIn, xIn, yIn, zIn, xOffsetIn, yOffsetIn, zOffsetIn, speedIn, countIn, argumentsIn);
        }
    }

    public static void givePlayerItemStack(EntityPlayer player, ItemStack stack) {
        if (player.getHeldItemMainhand().isEmpty())
            player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
        else if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, true);
        }
    }

    public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z) {
        int oldDimension = player.world.provider.getDimension();
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
        MinecraftServer server = ((EntityPlayerMP) player).world.getMinecraftServer();
        WorldServer worldServer = server.getWorld(dimension);
        player.addExperienceLevel(0);

        if (player.dimension != dimension)
            worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new CustomTeleporter(worldServer, x, y, z));
        player.setPositionAndUpdate(x, y, z);
        if (oldDimension == 1) {
            player.setPositionAndUpdate(x, y, z);
            worldServer.spawnEntity(player);
            worldServer.updateEntityWithOptionalForce(player, false);
        }
    }

    public static void swingPlayerArm(EntityPlayer player, EnumHand hand) {
        LCPacketDispatcher.sendToAll(new MessageSwingArm(player, hand));
    }

    @SideOnly(Side.CLIENT)
    public static boolean hasSmallArms(EntityPlayer player) {
        if (player instanceof AbstractClientPlayer)
            return ((AbstractClientPlayer) player).getSkinType().equalsIgnoreCase("slim");
        return false;
    }

    public static RayTraceResult rayTrace(EntityPlayer player, double distance) {
        Vec3d lookVec = player.getLookVec();
        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d pos = player.getPositionVector().add(0, player.getEyeHeight(), 0).add(lookVec.scale(scale));

            if (player.world.isBlockFullCube(new BlockPos(pos)) && !player.world.isAirBlock(new BlockPos(pos))) {
                return new RayTraceResult(pos, null);
            } else {
                Vec3d pos1 = pos.add(0.25F, 0.25F, 0.25F);
                Vec3d pos2 = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z))) {
                    return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(player.getPositionVector().add(lookVec.scale(distance)), null);
    }

}
