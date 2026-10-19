package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.abilities.AbilityPortal;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.helper.LCMathHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.particles.ParticleColoredCloud;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.Random;
import java.util.UUID;

public class EntityPortal extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Integer> OPENING = EntityDataManager.<Integer>createKey(EntityPortal.class, DataSerializers.VARINT);

    public float size;
    public int destinationDim;
    public Vec3d destination;
    protected boolean closing;
    public UUID partnerPortal;
    public boolean invasion = false;
    public static final int maxOpening = 3 * 20;

    public EntityPortal(World worldIn) {
        super(worldIn);
        this.height = 1F;
        this.width = 1F;
        this.ignoreFrustumCheck = true;
    }

    public EntityPortal(World worldIn, float size, int destinationDim, Vec3d destination) {
        this(worldIn);
        this.size = size;
        this.destinationDim = destinationDim;
        this.destination = destination;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (HEConfig.PORTAL_LIFETIME > -1 && this.ticksExisted >= HEConfig.PORTAL_LIFETIME)
            this.closing = true;

        if (!this.closing && this.getOpening() < maxOpening)
            this.setOpening(this.getOpening() + 1);
        else if (this.closing && this.getOpening() > 0)
            this.setOpening(this.getOpening() - 1);
        else if (this.getOpening() == 0)
            this.setDead();

        if (!this.world.isRemote && this.destination != null && !invasion) {
            for (Entity entity : this.world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox())) {
                if (!(entity instanceof EntityPortal) && entity.timeUntilPortal == 0) {
                    entity.timeUntilPortal = 5 * 20;
                    PlayerHelper.playSoundToAll(world, entity.posX, entity.posY, entity.posZ, 50, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.AMBIENT);
                    if (this.destinationDim != entity.dimension)
                        entity.changeDimension(this.destinationDim, (world, en, yaw) -> {
                            en.setLocationAndAngles(this.destination.x, this.destination.y, this.destination.z, en.rotationYaw, en.rotationPitch);
                        });
                    else
                        entity.setPositionAndUpdate(this.destination.x, this.destination.y, this.destination.z);
                    createPortalPortalIfNotExist();
                }
            }
        }

        if (this.world.isRemote && this.ticksExisted % 2 == 0) {
            float radius = (this.size / 2F) * ((float) this.getOpening() / (float) maxOpening);
            int corners = 10;
            Vec3d vec = new Vec3d(radius, 0, 0);
            Vec3d middle = new Vec3d(0, 0, 0);

            for (int i = 0; i < corners; i++) {
                Vec3d pos = middle.add(LCMathHelper.rotateZ(vec, (float) Math.toRadians((360D / (double) corners) * (double) i)));
                Vec3d pos2 = middle.add(LCMathHelper.rotateZ(vec, (float) Math.toRadians((360D / (double) corners) * (double) (i + 1))));
                Vec3d diff = pos2.subtract(pos);

                for (int j = 0; j < 10; j++) {
                    Vec3d v = pos.add(diff.scale(0.1D * (double) j));
                    v = v.rotatePitch((float) Math.toRadians(-this.rotationPitch));
                    v = v.rotateYaw((float) Math.toRadians(-this.rotationYaw));
                    Vec3d speed = v.scale(0D);
                    v = this.getPositionVector().add(0, this.height / 2F, 0).add(v);

                    double brightness = new Random().nextFloat();
                    // Bright: 176, 230, 255
                    // Dark: 0, 134, 198
                    LucraftCore.proxy.spawnParticle(ParticleColoredCloud.ID, v.x, v.y, v.z, speed.x, speed.y, speed.z, (int) (brightness * 176), 134 + (int) (brightness * 96), 198 + (int) (brightness * 57));
                }
            }
        }

        if (!this.getEntityWorld().isRemote && this.invasion && this.getEntityWorld().getDifficulty() != EnumDifficulty.PEACEFUL) {
            EnumDifficulty difficulty = this.getEntityWorld().getDifficulty();
            int minutes = difficulty == EnumDifficulty.EASY ? 4 : (difficulty == EnumDifficulty.NORMAL ? 3 : 1);

            if (this.ticksExisted == maxOpening || this.ticksExisted % (minutes * 20 * 60) == 0) {
                EntityLeviathan leviathan = new EntityLeviathan(this.getEntityWorld());
                leviathan.setPosition(this.posX, this.posY, this.posZ);
                leviathan.motionY = -10;
                leviathan.portal = this.getPersistentID();
                this.getEntityWorld().spawnEntity(leviathan);
            }
        }
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        if (entityIn instanceof EntityPlayer && Ability.hasAbility((EntityLivingBase) entityIn, AbilityPortal.class)) {
            closePortal();
        }

        return super.hitByEntity(entityIn);
    }

    public void closePortal() {
        this.closing = true;
        EntityPortal partner = getPartnerPortal();
        if (partner != null)
            partner.closing = true;
    }

    public boolean isClosing() {
        return this.closing;
    }

    public int getOpening() {
        return this.dataManager.get(OPENING);
    }

    public void setOpening(int i) {
        this.dataManager.set(OPENING, i);
    }

    public EntityPortal getPartnerPortal() {
        if (this.partnerPortal == null)
            return null;

        WorldServer world = DimensionManager.getWorld(this.destinationDim);

        if (world != null) {
            Entity entity = LCEntityHelper.getEntityByUUID(world, this.partnerPortal);

            if (entity != null && entity instanceof EntityPortal)
                return (EntityPortal) entity;
        }

        return null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public float getEyeHeight() {
        return this.height * 0.5F;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(OPENING, Integer.valueOf(0));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.size = compound.getFloat("Size");
        this.setOpening(compound.getInteger("Opening"));
        this.closing = compound.getBoolean("Closing");
        this.destinationDim = compound.getInteger("DestinationDim");
        this.destination = new Vec3d(compound.getDouble("DestinationX"), compound.getDouble("DestinationY"), compound.getDouble("DestinationZ"));
        this.invasion = compound.getBoolean("Invasion");
        if (compound.hasKey("PartnerPortal"))
            this.partnerPortal = UUID.fromString(compound.getString("PartnerPortal"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("Size", this.size);
        compound.setInteger("Opening", getOpening());
        compound.setBoolean("Closing", this.closing);
        compound.setInteger("DestinationDim", this.destinationDim);
        compound.setDouble("DestinationX", this.destination.x);
        compound.setDouble("DestinationY", this.destination.y);
        compound.setDouble("DestinationZ", this.destination.z);
        compound.setBoolean("Invasion", this.invasion);
        if (partnerPortal != null)
            compound.setString("PartnerPortal", this.partnerPortal.toString());
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeEntityToNBT(nbt);
        ByteBufUtils.writeTag(buffer, nbt);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.readEntityFromNBT(ByteBufUtils.readTag(additionalData));
    }

    public void createPortalPortalIfNotExist() {
        WorldServer w = DimensionManager.getWorld(destinationDim);
        if (w != null) {
            for (EntityPortal portals : w.getEntitiesWithinAABB(EntityPortal.class, new AxisAlignedBB(this.destination.x - 1, this.destination.y - 1, this.destination.z - 1, this.destination.x + 1, this.destination.y + 1, this.destination.z + 1))) {
                return;
            }

            EntityPortal portal2 = new EntityPortal(w, this.size, world.provider.getDimension(), this.getPositionVector());
            portal2.setLocationAndAngles(destination.x, destination.y, destination.z, rotationYaw, rotationPitch);
            w.spawnEntity(portal2);
            this.partnerPortal = portal2.getPersistentID();
            portal2.partnerPortal = this.getPersistentID();
            PlayerHelper.playSoundToAll(portal2.world, portal2.posX, portal2.posY, portal2.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
        }
    }

    public static EntityPortal createPortal(World world, Vec3d pos, float rotationYaw, float rotationPitch, int destinationDim, Vec3d destination, float size) {
        EntityPortal portal1 = new EntityPortal(world, size, destinationDim, destination);
        portal1.setLocationAndAngles(pos.x, pos.y, pos.z, rotationYaw, rotationPitch);
        world.spawnEntity(portal1);
        PlayerHelper.playSoundToAll(portal1.world, portal1.posX, portal1.posY, portal1.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
        WorldServer w = DimensionManager.getWorld(destinationDim);

        if (w != null) {
            EntityPortal portal2 = new EntityPortal(w, size, world.provider.getDimension(), pos);
            portal2.setLocationAndAngles(destination.x, destination.y, destination.z, rotationYaw, rotationPitch);
            w.spawnEntity(portal2);
            portal2.partnerPortal = portal1.getPersistentID();
            portal1.partnerPortal = portal2.getPersistentID();
            PlayerHelper.playSoundToAll(portal2.world, portal2.posX, portal2.posY, portal2.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
        }
        return portal1;
    }

    public static EntityPortal createPortal(World world, Vec3d pos, float rotationYaw, float rotationPitch, int destinationDim, Vec3d destination) {
        return createPortal(world, pos, rotationYaw, rotationPitch, destinationDim, destination, 3F);
    }

    public static EntityPortal createInvasionPortal(World world, Vec3d pos, float rotationYaw, float rotationPitch, float size) {
        EntityPortal portal1 = new EntityPortal(world, size, 0, Vec3d.ZERO);
        portal1.setLocationAndAngles(pos.x, pos.y, pos.z, rotationYaw, rotationPitch);
        portal1.invasion = true;
        world.spawnEntity(portal1);
        PlayerHelper.playSoundToAll(portal1.world, portal1.posX, portal1.posY, portal1.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
        return portal1;
    }

}