package lucraft.mods.lucraftcore.superpowers.entities;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.awt.*;

public class EntityEnergyBlast extends EntityThrowable implements IEntityAdditionalSpawnData {

    public float damage;
    public Color color;
    public int maxLife = 30 * 20;

    public EntityEnergyBlast(World worldIn) {
        super(worldIn);
    }

    public EntityEnergyBlast(World worldIn, EntityLivingBase throwerIn, float damage, Color color) {
        super(worldIn, throwerIn);
        this.damage = damage;
        this.color = color;
    }

    @Override
    public void onEntityUpdate() {
        double speed = new Vec3d(posX, posY, posZ).distanceTo(new Vec3d(prevPosX, prevPosY, prevPosZ));
        if (!this.world.isRemote && (ticksExisted > this.maxLife || speed < 0.01)) {
            this.setDead();
        }
        super.onEntityUpdate();
    }

    @Override
    public void shoot(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {
        super.shoot(entityThrower, rotationPitchIn, rotationYawIn, pitchOffset, velocity, inaccuracy);
        if(entityThrower instanceof EntityLivingBase) {
            this.thrower = (EntityLivingBase) entityThrower;
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result == null || isDead)
            return;

        if (result.typeOfHit == Type.ENTITY) {
            if (result.entityHit == this.thrower) return;
            result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), damage);
        } else if (result.typeOfHit == Type.BLOCK) {
            PlayerHelper.spawnParticleForAll(getEntityWorld(), 50, EnumParticleTypes.SMOKE_NORMAL, true, (float) this.posX, (float) this.posY, (float) this.posZ, 0, 0, 0, 0.01F, 5);
        }

        if (!this.world.isRemote)
            this.setDead();
    }

    @Override
    protected float getGravityVelocity() {
        return 0.00001F;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("Damage", damage);
        compound.setInteger("Color_R", color.getRed());
        compound.setInteger("Color_G", color.getGreen());
        compound.setInteger("Color_B", color.getBlue());
        compound.setBoolean("Invisible", this.isInvisible());
        compound.setInteger("Lifetime", this.maxLife);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.damage = compound.getFloat("Damage");
        this.color = new Color(compound.getInteger("Color_R"), compound.getInteger("Color_G"), compound.getInteger("Color_B"));
        this.setInvisible(compound.getBoolean("Invisible"));
        this.maxLife = compound.getInteger("Lifetime");
        if (this.maxLife <= 0) {
            this.maxLife = 30 * 20;
        }
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

    @Override
    public boolean isInWater() {
        return false;
    }
}
