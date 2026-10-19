package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public abstract class EntityWebShoot extends EntityThrowable implements IEntityAdditionalSpawnData {

    public float length;
    public float distance;
    public boolean prevOnGround;
    public EntityLivingBase attachedEntity;
    public BlockPos attachedBlock;
    public int lifeTime = 1200;

    public EntityWebShoot(World worldIn) {
        super(worldIn);
        this.ignoreFrustumCheck = true;
        this.setSize(0.1F, 0.1F);
    }

    public EntityWebShoot(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
        this.ignoreFrustumCheck = true;
        this.setSize(0.1F, 0.1F);
    }

    public EntityWebShoot(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
        this.ignoreFrustumCheck = true;
        this.setSize(0.1F, 0.1F);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01F;
    }

    public static boolean isWorldTheSame(World webWorld, World shooterWorld) {
        return webWorld.provider.getDimension() == shooterWorld.provider.getDimension();
    }

    public void pullThrowerBack() {
        if (distance > this.length + 0.5F) {
            float f = 30F;
            getThrower().motionX += (this.posX - getThrower().posX) / f;
            getThrower().motionY += (this.posY - getThrower().posY) / f;
            getThrower().motionZ += (this.posZ - getThrower().posZ) / f;

            if (getThrower() instanceof EntityPlayerMP)
                ((EntityPlayerMP) getThrower()).connection.sendPacket(new SPacketEntityVelocity(getThrower()));
        }
    }

    public void pullAttachedEntityBack() {
        if (getAttachedEntity() != null && getThrower() != null) {
            double distance = getThrower().getDistance(getAttachedEntity());

            if (distance > length) {
                getAttachedEntity().motionX += (getThrower().posX - getAttachedEntity().posX) / 20F;
                getAttachedEntity().motionY += (getThrower().posY - getAttachedEntity().posY) / 20F;
                getAttachedEntity().motionZ += (getThrower().posZ - getAttachedEntity().posZ) / 20F;
            }
        }

    }

    public float getHorizontalDistance(Vec3d pos1, Vec3d pos2) {
        float x = (float) (pos1.x - pos2.x);
        float z = (float) (pos1.z - pos2.z);
        return MathHelper.sqrt(x * x + z * z);
    }

    public boolean canBeCut() {
        return true;
    }

    public void setAttachedTo(EntityLivingBase entity) {
        this.attachedEntity = entity;
        this.startRiding(entity);
    }

    public Entity getAttachedEntity() {
        return this.attachedEntity;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (getThrower() != null) {
            return super.getRenderBoundingBox().expand(getThrower().posX - this.posX, getThrower().posY - this.posY, getThrower().posZ - this.posZ).expand(1, 1, 1).expand(-1, -1, -1);
        }
        return super.getRenderBoundingBox();
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!onGround && !this.world.isRemote && getThrower() != null && result.typeOfHit != RayTraceResult.Type.MISS) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK && !this.world.isBlockFullCube(result.getBlockPos()))
                return;
            if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLivingBase && result.entityHit != getThrower() && !(result.entityHit instanceof EntityWebShoot))
                this.setAttachedTo((EntityLivingBase) result.entityHit);
            if (result.typeOfHit == RayTraceResult.Type.BLOCK)
                this.attachedBlock = result.getBlockPos();

            this.motionX = this.motionY = this.motionZ = 0F;
            this.setPositionAndUpdate(result.hitVec.x, result.hitVec.y, result.hitVec.z);
            this.onGround = true;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("PrevOnGround", this.prevOnGround);
        compound.setInteger("LifeTime", this.lifeTime);
        if (this.attachedBlock != null)
            compound.setTag("AttachedBlock", NBTUtil.createPosTag(this.attachedBlock));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.prevOnGround = compound.getBoolean("PrevOnGround");
        this.lifeTime = compound.getInteger("LifeTime");
        if (compound.hasKey("AttachedBlock"))
            this.attachedBlock = NBTUtil.getPosFromTag(compound.getCompoundTag("AttachedBlock"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender() {
        if (getThrower() != null)
            return getThrower().getBrightnessForRender();
        return super.getBrightnessForRender();
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(this.getThrower() != null);
        if (this.getThrower() != null)
            buffer.writeInt(this.getThrower().getEntityId());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if (additionalData.readBoolean()) {
            Entity entity = this.world.getEntityByID(additionalData.readInt());
            if (entity != null && entity instanceof EntityLivingBase)
                this.thrower = (EntityLivingBase) entity;
        }
    }

    public static class EntityPullWeb extends EntityWebShoot {

        public EntityPullWeb(World worldIn) {
            super(worldIn);
        }

        public EntityPullWeb(World worldIn, double x, double y, double z) {
            super(worldIn, x, y, z);
        }

        public EntityPullWeb(World worldIn, EntityLivingBase throwerIn) {
            super(worldIn, throwerIn);
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();

            if (onGround && getThrower() != null && !this.world.isRemote) {

                if (getAttachedEntity() != null)
                    this.pullAttachedEntityBack();
                else {
                    if (!getThrower().onGround && getThrower() instanceof EntityPlayerMP) {
                        getThrower().motionX += (this.posX - getThrower().posX) / 50F;
                        getThrower().motionY += (this.posY - getThrower().posY) / 50F;
                        getThrower().motionZ += (this.posZ - getThrower().posZ) / 50F;
                        getThrower().fallDistance = 0F;

                        ((EntityPlayerMP) getThrower()).connection.sendPacket(new SPacketEntityVelocity(getThrower()));
                    } else {
                        pullThrowerBack();
                    }
                }
            }
        }
    }

    public static class EntitySwingWeb extends EntityWebShoot {

        public EntitySwingWeb(World worldIn) {
            super(worldIn);
        }

        public EntitySwingWeb(World worldIn, double x, double y, double z) {
            super(worldIn, x, y, z);
        }

        public EntitySwingWeb(World worldIn, EntityLivingBase throwerIn) {
            super(worldIn, throwerIn);
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();

            if (onGround && getThrower() != null && !this.world.isRemote) {
                float distance = this.getDistance(getThrower());

                if (getAttachedEntity() != null)
                    this.pullAttachedEntityBack();
                else {
                    if (!getThrower().onGround && getThrower().onGround != this.prevOnGround) {
                        double u = 2F * Math.PI * this.length;
                        double s = getHorizontalDistance(this.getPositionVector(), this.getThrower().getPositionVector());
                        double alpha = Math.toDegrees(Math.asin(s / distance));
                        this.distance = (float) (u * (alpha / 180F)) * 1.5F;
                    }

                    if (!getThrower().onGround && this.distance > 0F) {
                        float g = getThrower().posY > this.posY ? ((float) ((getThrower().posY - this.posY) / this.length) * 5F) + 1F : 1F;
                        Vec3d lookVec = getThrower().getLookVec();
                        getThrower().motionX = lookVec.x / g;
                        getThrower().motionY = lookVec.y / g;
                        getThrower().motionZ = lookVec.z / g;
                        Vec3d vec = getThrower().getPositionVector().subtract(this.getPositionVector());
                        vec = vec.normalize().scale(this.length);
                        vec = this.getPositionVector().add(vec);
                        float f = 5F / g;
                        getThrower().motionX += (vec.x - getThrower().posX) / f;
                        getThrower().motionY += (vec.y - getThrower().posY) / f;
                        getThrower().motionZ += (vec.z - getThrower().posZ) / f;
                        getThrower().fallDistance = 0F;

                        double deltaDistance = (getThrower().distanceWalkedModified - getThrower().prevDistanceWalkedModified) / 0.6F;
                        this.distance -= deltaDistance;
                        if (getThrower() instanceof EntityPlayerMP)
                            ((EntityPlayerMP) getThrower()).connection.sendPacket(new SPacketEntityVelocity(getThrower()));
                    } else {
                        pullThrowerBack();
                    }
                }

                this.prevOnGround = getThrower().onGround;
            }
        }
    }

    public static class EntityTaserWeb extends EntityWebShoot {

        public EntityTaserWeb(World worldIn) {
            super(worldIn);
        }

        public EntityTaserWeb(World worldIn, double x, double y, double z) {
            super(worldIn, x, y, z);
        }

        public EntityTaserWeb(World worldIn, EntityLivingBase throwerIn) {
            super(worldIn, throwerIn);
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();

            if (onGround && getThrower() != null && !this.world.isRemote) {
                this.pullThrowerBack();

                if (getAttachedEntity() != null) {
                    this.pullAttachedEntityBack();

                    if (this.ticksExisted % 10 == 0)
                        getAttachedEntity().attackEntityFrom(DamageSource.causeIndirectDamage(this, getThrower()), 2);
                }
            }
        }
    }

    public static class EntityImpactWeb extends EntityWebShoot {

        public EntityImpactWeb(World worldIn) {
            super(worldIn);
            this.lifeTime = 400;
        }

        public EntityImpactWeb(World worldIn, double x, double y, double z) {
            super(worldIn, x, y, z);
            this.lifeTime = 400;
        }

        public EntityImpactWeb(World worldIn, EntityLivingBase throwerIn) {
            super(worldIn, throwerIn);
            this.lifeTime = 400;
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();

            if (this.getAttachedEntity() != null && this.getAttachedEntity() instanceof EntityLivingBase) {
                ((EntityLivingBase) this.getAttachedEntity()).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 6, false, false));
            }
        }

        @Override
        public void setAttachedTo(EntityLivingBase entity) {
            boolean b = false;

            for (Entity en : entity.getPassengers()) {
                if (en instanceof EntityImpactWeb)
                    b = true;
            }

            if (!b)
                super.setAttachedTo(entity);
        }

        @Override
        public boolean canBeCut() {
            return false;
        }
    }

    public static class EntityWebGrenade extends EntityWebShoot {

        public EntityWebGrenade(World worldIn) {
            super(worldIn);
        }

        public EntityWebGrenade(World worldIn, double x, double y, double z) {
            super(worldIn, x, y, z);
        }

        public EntityWebGrenade(World worldIn, EntityLivingBase throwerIn) {
            super(worldIn, throwerIn);
        }

        @Override
        protected void onImpact(RayTraceResult result) {
            if (!this.world.isRemote && !this.isDead) {
                this.setDead();
                Random rand = new Random();
                for (int i = 0; i < 9; i++) {
                    EntityImpactWeb entity = new EntityImpactWeb(this.world, this.posX, this.posY, this.posZ);
                    float f = 0.4F;
                    entity.motionX = (rand.nextFloat() - 0.5F) * f;
                    entity.motionY = (rand.nextFloat() * 0.5F) * f;
                    entity.motionZ = (rand.nextFloat() - 0.5F) * f;
                    entity.lifeTime = 200;
                    if (getThrower() != null)
                        entity.thrower = getThrower();
                    this.world.spawnEntity(entity);
                }
            }
        }
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if (this.world.isRemote) {
            this.noClip = false;
        } else {
            this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }

        if (this.onGround) {
            this.motionX = this.motionY = this.motionZ = 0F;

            if (getThrower() != null && !this.world.isRemote) {
                float distance = this.getDistance(getThrower());
                if (this.length <= 0F)
                    this.length = distance;

                if (getThrower().isSneaking()) {
                    this.length = MathHelper.clamp(this.length - 0.2F, 0.1F, Float.MAX_VALUE);
                }
            }

            if (this.getAttachedEntity() != null) {
                this.setPosition(this.getAttachedEntity().posX, this.getAttachedEntity().posY + this.getAttachedEntity().height / 2F, this.getAttachedEntity().posZ);
            }
        }

        this.lifeTime--;

        if (getThrower() == null || getThrower().isDead || posY < 0 || (getAttachedEntity() != null && getAttachedEntity().isDead) || this.lifeTime <= 0 || getThrower().getDistance(this) > 60 || !isWorldTheSame(this.world, getThrower().world) || (this.attachedBlock != null && !this.world.isBlockFullCube(this.attachedBlock)))
            this.setDead();
    }


}