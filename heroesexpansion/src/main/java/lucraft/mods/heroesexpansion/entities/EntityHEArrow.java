package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.items.ItemHEArrow;
import lucraft.mods.heroesexpansion.items.ItemKryptonite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.Random;
import java.util.UUID;

public abstract class EntityHEArrow extends EntityArrow implements IEntityAdditionalSpawnData {

    public int color = 2521149;

    public EntityHEArrow(World worldIn) {
        super(worldIn);
    }

    public EntityHEArrow(World worldIn, EntityLivingBase shooter, int color) {
        super(worldIn, shooter);
        this.color = color;
    }

    public EntityHEArrow(World worldIn, double x, double y, double z, int color) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.color = color;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.color = nbt.getInteger("Color");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("Color", color);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeEntityToNBT(nbt);
        ByteBufUtils.writeTag(buffer, nbt);
        buffer.writeBoolean(shootingEntity != null);
        if (shootingEntity != null)
            ByteBufUtils.writeUTF8String(buffer, this.shootingEntity.getPersistentID().toString());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        this.readEntityFromNBT(ByteBufUtils.readTag(additionalData));
        if (additionalData.readBoolean())
            this.shootingEntity = this.getEntityWorld().getPlayerEntityByUUID(UUID.fromString(ByteBufUtils.readUTF8String(additionalData)));
    }

    public static class EntityExplosiveArrow extends EntityHEArrow {

        public EntityExplosiveArrow(World worldIn) {
            super(worldIn);
        }

        public EntityExplosiveArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
        }

        public EntityExplosiveArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            if (!this.isDead && !this.getEntityWorld().isRemote) {
                boolean damage = this.getEntityWorld().getGameRules().getBoolean("mobGriefing") && this.shootingEntity instanceof EntityPlayer && !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(this.world, this.getPosition(), this.world.getBlockState(this.getPosition()), (EntityPlayer) shootingEntity));
                this.getEntityWorld().newExplosion(shootingEntity, posX, posY, posZ, (float) HEConfig.EXPLOSIVE_ARROW_STRENGTH, false, damage);
                this.setDead();
            }
            super.onHit(rtr);
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.EXPLOSIVE_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

    public static class EntitySharpenedArrow extends EntityHEArrow {

        public EntitySharpenedArrow(World worldIn) {
            super(worldIn);
            this.setDamage(5);
        }

        public EntitySharpenedArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
            this.setDamage(5);
        }

        public EntitySharpenedArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            super.onHit(rtr);
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.SHARPENED_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

    public static class EntitySmokeArrow extends EntityHEArrow {

        public EntitySmokeArrow(World worldIn) {
            super(worldIn);
        }

        public EntitySmokeArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
        }

        public EntitySmokeArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            super.onHit(rtr);
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();
            this.pickupStatus = PickupStatus.DISALLOWED;

            if (inGround) {
                for (int i = 0; i < 20; i++) {
                    Random rand = new Random();
                    double x = this.posX + (rand.nextDouble() - 0.5D) * 3D;
                    double y = this.posY + rand.nextDouble() * 2D;
                    double z = this.posZ + (rand.nextDouble() - 0.5D) * 3D;
                    double oX = 0;
                    double oY = 0.01F;
                    double oZ = 0;
                    if (i % 8 == 0)
                        this.getEntityWorld().spawnAlwaysVisibleParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), x, y, z, oX, oY, oZ);
                    this.getEntityWorld().spawnAlwaysVisibleParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), x, y, z, oX, oY, oZ);
                }
            }
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.SMOKE_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

    public static class EntityGasArrow extends EntityHEArrow {

        public EntityGasArrow(World worldIn) {
            super(worldIn);
        }

        public EntityGasArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
        }

        public EntityGasArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();
            this.pickupStatus = PickupStatus.DISALLOWED;

            if (inGround) {
                double radius = 1.5D;

                for (EntityLivingBase entity : this.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.getPosition().add(radius, radius, radius), this.getPosition().add(-radius, -radius, -radius)))) {
                    entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20, 1));
                }

                for (int i = 0; i < 20; i++) {
                    Random rand = new Random();
                    double x = this.posX + (rand.nextDouble() - 0.5D) * 3D;
                    double y = this.posY + rand.nextDouble() * 2D;
                    double z = this.posZ + (rand.nextDouble() - 0.5D) * 3D;
                    double oX = 0;
                    double oY = 0.01F;
                    double oZ = 0;
                    this.getEntityWorld().spawnAlwaysVisibleParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), x, y, z, oX, oY, oZ);
                    if (i % 8 == 0)
                        this.getEntityWorld().spawnAlwaysVisibleParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), x, y, z, oX, oY, oZ);
                }
            }
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.GAS_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

    public static class EntityGrapplingHookArrow extends EntityHEArrow {

        public boolean landed;

        public EntityGrapplingHookArrow(World worldIn) {
            super(worldIn);
            this.ignoreFrustumCheck = true;
        }

        public EntityGrapplingHookArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
            this.ignoreFrustumCheck = true;
        }

        public EntityGrapplingHookArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
            this.ignoreFrustumCheck = true;
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            Entity entity = rtr.entityHit;
            if (!landed) {
                if (entity != null) {

                    if (this.isBurning() && !(entity instanceof EntityEnderman)) {
                        entity.setFire(5);
                    }

                    if (entity != shootingEntity)
                        this.startRiding(entity);

                } else {
                    super.onHit(rtr);
                }

                this.landed = true;
            }
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.GRAPPLING_HOOK_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();
            this.pickupStatus = PickupStatus.DISALLOWED;

            if (landed && !this.isDead) {
                if (getRidingEntity() != null && shootingEntity != null) {
                    if (shootingEntity.getDistance(getRidingEntity()) > 5) {
                        getRidingEntity().motionX += (shootingEntity.posX - getRidingEntity().posX) / 50F;
                        getRidingEntity().motionY += (shootingEntity.posY - getRidingEntity().posY) / 50F;
                        getRidingEntity().motionZ += (shootingEntity.posZ - getRidingEntity().posZ) / 50F;
                        getRidingEntity().fallDistance = 0F;
                    }

                    if (this.ticksExisted > 1200)
                        this.setDead();
                } else if (shootingEntity != null && !world.isRemote) {
                    shootingEntity.motionX += (this.posX - shootingEntity.posX) / 50F;
                    shootingEntity.motionY += (this.posY - shootingEntity.posY) / 50F;
                    shootingEntity.motionZ += (this.posZ - shootingEntity.posZ) / 50F;
                    shootingEntity.fallDistance = 0F;

                    ((EntityPlayerMP) shootingEntity).connection.sendPacket(new SPacketEntityVelocity(shootingEntity));

                    if (this.getDistance(shootingEntity) > 50F)
                        this.setDead();
                }
            }

        }

    }

    public static class EntityKryptoniteArrow extends EntityHEArrow {

        public EntityKryptoniteArrow(World worldIn) {
            super(worldIn);
            this.setDamage(5);
        }

        public EntityKryptoniteArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
            this.setDamage(5);
        }

        public EntityKryptoniteArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            super.onHit(rtr);

            if (rtr.typeOfHit == RayTraceResult.Type.ENTITY && rtr.entityHit != null && rtr.entityHit instanceof EntityPlayer) {
                ItemKryptonite.giveKryptonitePoison((EntityPlayer) rtr.entityHit, 10 * 20);
            }
        }

        @Override
        public void onEntityUpdate() {
            super.onEntityUpdate();
            float range = 2.5F;

            for (EntityPlayer player : this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - range, this.posY - range, this.posZ + range, this.posX + range, this.posY + range, this.posZ - range))) {
                ItemKryptonite.giveKryptonitePoison((EntityPlayer) player, 5 * 20);
            }
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.KRYPTONITE_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

    public static class EntityVibraniumArrow extends EntityHEArrow {

        public EntityVibraniumArrow(World worldIn) {
            super(worldIn);
            this.setDamage(15);
        }

        public EntityVibraniumArrow(World worldIn, EntityLivingBase shooter, int color) {
            super(worldIn, shooter, color);
            this.setDamage(15);
        }

        public EntityVibraniumArrow(World worldIn, double x, double y, double z, int color) {
            super(worldIn, x, y, z, color);
        }

        @Override
        protected void onHit(RayTraceResult rtr) {
            super.onHit(rtr);
        }

        @Override
        protected ItemStack getArrowStack() {
            ItemStack stack = new ItemStack(HEItems.VIBRANIUM_ARROW);
            ((ItemHEArrow) stack.getItem()).setColor(stack, this.color);
            return stack;
        }

    }

}
