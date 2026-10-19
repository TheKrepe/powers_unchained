package lucraft.mods.heroesexpansion.network;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.HEPermissions;
import lucraft.mods.heroesexpansion.entities.EntityPortal;
import lucraft.mods.heroesexpansion.events.OpenSpaceStonePortalEvent;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.server.permission.PermissionAPI;

public class MessageOpenPortal implements IMessage {

    public int dim;
    public double x;
    public double y;
    public double z;
    public int tileX;
    public int tileY = -1;
    public int tileZ;
    public boolean invasion = false;

    public MessageOpenPortal() {
    }

    public MessageOpenPortal(int dim, double x, double y, double z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tileY = -1;
        this.invasion = false;
    }

    public MessageOpenPortal(int dim, double x, double y, double z, int tileX, int tileY, int tileZ) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileZ = tileZ;
    }

    public MessageOpenPortal(int dim, double x, double y, double z, int tileX, int tileY, int tileZ, boolean invasion) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileZ = tileZ;
        this.invasion = invasion;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dim = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.tileX = buf.readInt();
        this.tileY = buf.readInt();
        this.tileZ = buf.readInt();
        this.invasion = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.dim);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeInt(this.tileX);
        buf.writeInt(this.tileY);
        buf.writeInt(this.tileZ);
        buf.writeBoolean(this.invasion);
    }

    public static class Handler extends AbstractServerMessageHandler<MessageOpenPortal> {

        @Override
        public IMessage handleServerMessage(EntityPlayer player, MessageOpenPortal message, MessageContext ctx) {

            LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(() -> {
                if (!PermissionAPI.hasPermission(player, HEPermissions.SPACE_STONE_PORTAL)) {
                    player.sendStatusMessage(new TextComponentTranslation("lucraftcore.info.no_perms"), true);
                    return;
                }

                if (message.invasion) {
                    player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.set_invasion"), true);
                } else {
                    try {
                        DimensionType dim = DimensionType.getById(message.dim);
                        player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.portal_location", "" + TextFormatting.YELLOW + message.x, "" + TextFormatting.YELLOW + message.y, "" + TextFormatting.YELLOW + message.z, TextFormatting.YELLOW + dim.getName().toUpperCase()), true);
                    } catch (Exception e) {
                        player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.dim_not_exist"), true);
                        return;
                    }

                    if (player.dimension == message.dim && !player.world.getWorldBorder().contains(new BlockPos(message.x, message.y, message.z))) {
                        player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.outside_worldborder"), true);
                        return;
                    }
                }

                if (message.tileY == -1) {
                    float distance = 3;
                    Vec3d pos = player.getPositionVector().add(0, player.eyeHeight, 0).add(player.getLookVec().scale(distance));

                    if (MinecraftForge.EVENT_BUS.post(new OpenSpaceStonePortalEvent(player, player.world, pos, message.dim, new Vec3d(message.x, message.y, message.z), false)))
                        return;

                    double deltaX = player.posX - pos.x;
                    double deltaY = (player.posY + player.getEyeHeight()) - (pos.y + 1.5F);
                    double deltaZ = player.posZ - pos.z;
                    double d = player.getPositionVector().add(0, player.getEyeHeight(), 0).distanceTo(pos.add(0, 1.5F, 0));
                    double d1 = deltaX / deltaZ;
                    double d2 = deltaY / d;
                    float rotationYaw = (float) -Math.toDegrees(Math.atan(d1));
                    float rotationPitch = (float) -Math.toDegrees(Math.asin(d2));
                    if (deltaZ < 0)
                        rotationYaw += 180;

                    EntityPortal.createPortal(player.world, pos, rotationYaw, rotationPitch, message.dim, new Vec3d(message.x, message.y, message.z));
                    PlayerHelper.swingPlayerArm(player, EnumHand.MAIN_HAND);
                } else {
                    BlockPos pos = new BlockPos(message.tileX, message.tileY, message.tileZ);

                    if (player.world.getTileEntity(pos) != null && player.world.getTileEntity(pos) instanceof TileEntityPortalDevice) {
                        TileEntityPortalDevice tileEntity = (TileEntityPortalDevice) player.world.getTileEntity(pos);

                        if (message.invasion) {
                            tileEntity.destination = TileEntityPortalDevice.EnumPortalDeviceDestination.INVASION;
                        } else {
                            tileEntity.destination = TileEntityPortalDevice.EnumPortalDeviceDestination.POSITION;
                            tileEntity.destinationDim = message.dim;
                            tileEntity.destinationPos = new Vec3d(message.x, message.y, message.z);
                        }
                        tileEntity.markDirty();
                    }
                }
            });

            return null;
        }
    }

}