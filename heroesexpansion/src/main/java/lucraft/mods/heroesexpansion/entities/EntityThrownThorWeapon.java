package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.util.items.IEntityItemTickable;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityThrownThorWeapon extends EntityThrowable implements IEntityAdditionalSpawnData {

    public ItemStack item;
    public boolean backToUser;
    public BlockPos location;

    public EntityThrownThorWeapon(World worldIn) {
        super(worldIn);
        this.setSize(0.5F, 0.5F);
    }

    public EntityThrownThorWeapon(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
        this.setSize(0.5F, 0.5F);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.0F;
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
            this.item.damageItem(1, getThrower());
            return;
        }

        float speed = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

        if (result.entityHit != null && result.entityHit != getThrower()) {
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), 5F);
            float dmg = this.item.getItem() instanceof ItemThorWeapon ? ((ItemThorWeapon) this.item.getItem()).material.getAttackDamage() : 10F;
            float sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, item) * 1.25F;
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), dmg + sharpness);

            if ((this.isBurning() && !(result.entityHit instanceof EntityEnderman))) {
                result.entityHit.setFire(5);
            }

            if ((EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, item) > 0 && !(result.entityHit instanceof EntityEnderman))) {
                result.entityHit.setFire(EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, item) * 4);
            }

            float knockbackStrength = 0.2F;
            result.entityHit.addVelocity(this.motionX * (double) knockbackStrength * 0.6000000238418579D / (double) speed, 0.1D, this.motionZ * (double) knockbackStrength * 0.6000000238418579D / (double) speed);

            if (result.entityHit instanceof EntityLivingBase)
                item.damageItem(1, (EntityLivingBase) result.entityHit);

            backToUser = true;
            this.backToUser = true;
        }

        if (result.typeOfHit == Type.BLOCK)
            this.backToUser = true;

        if (ticksExisted >= 20)
            this.backToUser = true;
    }

    @Override
    public void onEntityUpdate() {
        if (this.item.getItem() instanceof IEntityItemTickable && ((IEntityItemTickable) this.item.getItem()).onEntityItemTick(this.world, this, this.posX, this.posY, this.posZ))
            return;

        super.onEntityUpdate();

        if (getThrower() == null || getThrower().isDead)
            dropItem();

        if ((getThrower() != null && getDistance(this.getThrower()) >= 50) || this.ticksExisted >= 20 * 10 || this.posY < 0)
            backToUser = true;

        if (!isDead && backToUser && getThrower() != null && getThrower() instanceof EntityPlayer) {
            this.motionX = (this.getThrower().posX - posX) / 3D;
            this.motionY = (this.getThrower().posY - posY) / 3D;
            this.motionZ = (this.getThrower().posZ - posZ) / 3D;
            if (this.getDistance(getThrower()) <= 1.5F) {
                if (!this.world.isRemote) {
                    PlayerHelper.givePlayerItemStack((EntityPlayer) getThrower(), item);
                    PlayerHelper.playSoundToAll(getEntityWorld(), posX, posY, posZ, 30, HESoundEvents.HAMMER_HIT, SoundCategory.AMBIENT);
                    this.setDead();
                }
            }
        } else if (ticksExisted >= 20 && location != null) {
            // double d = this.getDistance(getThrower());
            // Vec3d v = getThrower().getLookVec().scale(d + 1.1D);
            // BlockPos pos = new BlockPos(getThrower().posX + v.x, getThrower().posY + v.y,
            // getThrower().posZ + v.z);
            //
            // this.motionX = (pos.getX() - posX) / 50D;
            // this.motionY = (pos.getY() - posY) / 50D;
            // this.motionZ = (pos.getZ() - posZ) / 50D;

            this.motionX = (location.getX() - posX) / 10D;
            this.motionY = (location.getY() - posY) / 10D;
            this.motionZ = (location.getZ() - posZ) / 10D;
            location = null;
        }
    }

    public void dropItem() {
        if (!getEntityWorld().isRemote) {
            this.world.spawnEntity(new EntityItem(getEntityWorld(), posX, posY, posZ, item));
            this.setDead();
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.item = new ItemStack(nbt.getCompoundTag("Item"));
        this.backToUser = nbt.getBoolean("BackToUser");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setTag("Item", this.item.writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("BackToUser", backToUser);
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

}
