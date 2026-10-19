package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.particles.ParticleColoredCloud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.Random;

public class EntityBlackHole extends Entity implements IEntityAdditionalSpawnData {

    private static final DataParameter<Integer> SIZE = EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.VARINT);
    public static final DamageSource BLACK_HOLE = new DamageSource("black_hole").setDamageBypassesArmor().setDamageIsAbsolute();
    public static final int MAX_SIZE = 120;

    public EntityLivingBase creator;
    public int prevSize;

    public EntityBlackHole(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntityBlackHole(World world, double posX, double posY, double posZ, EntityLivingBase creator) {
        this(world);
        this.setPositionAndUpdate(posX, posY, posZ);
        this.creator = creator;
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(SIZE, 0);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        this.prevSize = this.getSize();
        this.setSize(this.getSize() / 100F, this.getSize() / 100F);

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        this.setPositionAndUpdate(posX, posY, posZ);

        Random random = new Random();
        if (this.ticksExisted % 2 == 0) {
            double radius = this.getSize() / 20D;
            double x = this.posX + random.nextFloat() * radius * 2 - radius;
            double y = this.posY + this.height / 2D + random.nextFloat() * radius * 2 - radius;
            double z = this.posZ + random.nextFloat() * radius * 2 - radius;
            double xSpeed = (this.posX - x) / 20D;
            double ySpeed = (this.posY + this.width / 2D - y) / 20D;
            double zSpeed = (this.posZ - z) / 20D;
            double brightness = new Random().nextFloat();
            LucraftCore.proxy.spawnParticle(ParticleColoredCloud.ID, x, y, z, xSpeed, ySpeed, zSpeed, (int) (brightness * 176), 134 + (int) (brightness * 96), 198 + (int) (brightness * 57));
        }

        AxisAlignedBB box = this.getEntityBoundingBox().grow(getSize() / 20);
        for (Entity entity : this.world.getEntitiesWithinAABB(Entity.class, box, (e) -> e != this && e != this.creator)) {
            entity.motionX += (this.posX - entity.posX) / 10D;
            entity.motionY += (this.posY - entity.posY) / 10D;
            entity.motionZ += (this.posZ - entity.posZ) / 10D;

            if (entity.getDistance(this) < 0.5F) {
                entity.attackEntityFrom(BLACK_HOLE, Integer.MAX_VALUE);
            }
        }

        for (double x = box.minX; x < box.maxX; x++) {
            for (double y = box.minY; y < box.maxY; y++) {
                for (double z = box.minZ; z < box.maxZ; z++) {
                    Vec3d pos = new Vec3d(x, y, z);
                    if (pos.distanceTo(this.getPositionVector().add(0, this.height / 2D, 0)) <= this.width * 2F && LCEntityHelper.canDestroyBlock(this.world, new BlockPos(pos), this.creator))
                        this.world.destroyBlock(new BlockPos(pos), true);
                }
            }
        }

        this.setSize(this.getSize() - 1);

        if (this.getSize() <= 0 && this.ticksExisted > 20 && !this.world.isRemote)
            this.setDead();
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setSize(compound.getInteger("Size"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("Size", this.getSize());
    }

    public int getSize() {
        return this.getDataManager().get(SIZE);
    }

    public void setSize(int size) {
        this.getDataManager().set(SIZE, MathHelper.clamp(size, 0, MAX_SIZE));
    }

    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        double x = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        double y = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        double z = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double) f;
        y = y / (double) f;
        z = z / (double) f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        x = x * (double) velocity;
        y = y * (double) velocity;
        z = z * (double) velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround) {
            this.motionY += entityThrower.motionY;
        }
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.creator != null);
        if (this.creator != null)
            buffer.writeInt(this.creator.getEntityId());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if (additionalData.readBoolean()) {
            Entity entity = this.world.getEntityByID(additionalData.readInt());
            if (entity instanceof EntityLivingBase)
                this.creator = (EntityLivingBase) entity;
        }
    }
}
