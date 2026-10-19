package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.abilities.AbilityPrecision;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.util.items.IEntityItemTickable;
import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.UUID;

public class EntityCaptainAmericaShield extends EntityThrowable implements IEntityAdditionalSpawnData {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.<ItemStack>createKey(EntityCaptainAmericaShield.class, DataSerializers.ITEM_STACK);

    public boolean backToUser;

    public EntityCaptainAmericaShield(World worldIn) {
        super(worldIn);
        this.setSize(1F, 0.1F);
    }

    public EntityCaptainAmericaShield(World world, EntityLivingBase entity, ItemStack stack) {
        super(world, entity);
        this.setSize(1F, 0.1F);
        this.setItem(stack);
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void onEntityUpdate() {
        if (this.getItem().getItem() instanceof IEntityItemTickable && ((IEntityItemTickable) this.getItem().getItem()).onEntityItemTick(this.world, this, this.posX, this.posY, this.posZ))
            return;

        super.onEntityUpdate();

        if (getThrower() == null || getThrower().isDead)
            dropItem();

        if (ticksExisted > 20 * 10 || this.posY < 0 || world.isBlockFullCube(this.getPosition()) || (getThrower() != null && getDistance(this.getThrower()) >= 50) || this.posY < 0)
            backToUser = true;

        if (!isDead && backToUser && getThrower() != null && getThrower() instanceof EntityPlayer) {
            this.motionX = (this.getThrower().posX - posX) / 3D;
            this.motionY = (this.getThrower().posY + (((EntityPlayer) getThrower()).height / 2D) - posY) / 3D;
            this.motionZ = (this.getThrower().posZ - posZ) / 3D;

            if (this.getDistance(getThrower()) <= 1.5F) {
                if (!this.world.isRemote) {
                    PlayerHelper.givePlayerItemStack((EntityPlayer) getThrower(), getItem());
                    PlayerHelper.playSoundToAll(getEntityWorld(), posX, posY, posZ, 30, getPickupSound(), SoundCategory.AMBIENT);
                    this.setDead();
                }
            }
        }
    }

    public void dropItem() {
        if (!getEntityWorld().isRemote) {
            this.world.spawnEntity(new EntityItemIndestructible(getEntityWorld(), posX, posY, posZ, getItem()));
            this.setDead();
        }
    }

    public boolean canDestroyBlock(BlockPos pos) {
        if (this.getThrower() != null && this.getThrower() instanceof EntityPlayer) {
            return !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(this.world, pos, this.world.getBlockState(pos), (EntityPlayer) getThrower()));
        }

        return false;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (getEntityWorld().isRemote || isDead)
            return;

        if (getThrower() != null && result.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(result.getBlockPos()).getMaterial() == Material.GLASS && this.world.getBlockState(result.getBlockPos()).getBlockHardness(this.world, result.getBlockPos()) <= 0.5F && canDestroyBlock(result.getBlockPos())) {
            this.world.destroyBlock(result.getBlockPos(), true);
            getItem().damageItem(1, getThrower());
            return;
        }

        float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        PlayerHelper.playSoundToAll(getEntityWorld(), posX, posY, posZ, 30, getHitSound(), SoundCategory.AMBIENT, MathHelper.clamp(1F * f1, 0, 1), 1);

        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (result.entityHit != null && result.entityHit == getThrower() && getThrower() instanceof EntityPlayer) {
                PlayerHelper.givePlayerItemStack((EntityPlayer) getThrower(), getItem());
                this.setDead();
            } else {

                float dmg = this.getItem().getItem() instanceof ItemShield ? 2F : 10F;
                float sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, getItem()) * 1.25F;
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (dmg + sharpness) * MathHelper.clamp(f1, 0, 1F));

                if ((this.isBurning() && !(result.entityHit instanceof EntityEnderman))) {
                    result.entityHit.setFire(5);
                }

                if ((EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, getItem()) > 0 && !(result.entityHit instanceof EntityEnderman))) {
                    result.entityHit.setFire(EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, getItem()) * 4);
                }

                float knockbackStrength = 0.2F;
                result.entityHit.addVelocity(this.motionX * (double) knockbackStrength * 0.6000000238418579D / (double) f1, 0.1D, this.motionZ * (double) knockbackStrength * 0.6000000238418579D / (double) f1);

                if (result.entityHit instanceof EntityLivingBase)
                    getItem().damageItem(1, (EntityLivingBase) result.entityHit);

                backToUser = true;
                this.motionX *= -0.4D;
                this.motionY += 0.3D;
                this.motionZ *= -0.4D;
            }
        } else if (result.typeOfHit == RayTraceResult.Type.BLOCK && !backToUser) {
            if (!canUsePrecision() || !toClosestEntity()) {
                if (result.sideHit == EnumFacing.NORTH || result.sideHit == EnumFacing.SOUTH)
                    this.motionZ *= -0.2D;
                else if (result.sideHit == EnumFacing.EAST || result.sideHit == EnumFacing.WEST)
                    this.motionX *= -0.2D;
                else if (result.sideHit == EnumFacing.DOWN)
                    this.motionY *= -0.01D;
                else if (result.sideHit == EnumFacing.UP) {
                    backToUser = true;
                }
            }
        }
    }

    public boolean canUsePrecision() {
        if (getThrower() == null || !(getThrower() instanceof EntityPlayer))
            return false;

        for (AbilityPrecision ab : Ability.getAbilitiesFromClass(Ability.getAbilities((EntityPlayer) getThrower()), AbilityPrecision.class)) {
            if (ab != null && ab.isUnlocked())
                return true;
        }
        return false;
    }

    public boolean toClosestEntity() {
        EntityLivingBase entity = getClosestEntity(10);

        if (entity != null) {
            this.motionX = (entity.posX - posX) / 5D;
            this.motionY = ((entity.posY + entity.height / 2D) - posY) / 5D;
            this.motionZ = (entity.posZ - posZ) / 5D;
            return true;
        }

        return false;
    }

    public EntityLivingBase getClosestEntity(double radius) {
        EntityLivingBase entity = null;
        double distance = 0D;

        for (Entity en : this.world.getEntitiesInAABBexcluding(this, new AxisAlignedBB(this.posX - radius, this.posY - radius, this.posZ - radius, this.posX + radius, this.posY + radius, this.posZ + radius), (e) -> e != getThrower() && e instanceof EntityLivingBase && ((EntityLivingBase) e).canEntityBeSeen(this))) {
            double d = this.getDistance(en);

            if (d < distance || distance == 0D) {
                distance = d;
                entity = (EntityLivingBase) en;
            }
        }

        return entity;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.01F;
    }

    public ItemStack getItem() {
        return (ItemStack) this.getDataManager().get(ITEM);
    }

    public void setItem(ItemStack stack) {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.backToUser = nbt.getBoolean("BackToUser");
        NBTTagCompound nbttagcompound = nbt.getCompoundTag("Item");
        this.setItem(new ItemStack(nbttagcompound));

        if (this.getItem().isEmpty())
            this.setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("BackToUser", backToUser);
        if (!this.getItem().isEmpty())
            nbt.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));
    }

    public SoundEvent getHitSound() {
        return HESoundEvents.SHIELD_HIT;
    }

    public SoundEvent getPickupSound() {
        return HESoundEvents.SHIELD_HIT;
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeBoolean(getThrower() != null);
        if (getThrower() != null)
            ByteBufUtils.writeUTF8String(buffer, this.getThrower().getPersistentID().toString());
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        if (additionalData.readBoolean())
            this.thrower = this.getEntityWorld().getPlayerEntityByUUID(UUID.fromString(ByteBufUtils.readUTF8String(additionalData)));
    }
}
