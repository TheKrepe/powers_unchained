package lucraft.mods.heroes.speedsterheroes.entity;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import lucraft.mods.heroes.speedsterheroes.entity.EntityDimensionBreach.BreachActionTypes;
import lucraft.mods.heroes.speedsterheroes.util.SHAchievements;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityTimeRemnant extends EntityTameable {

	public int wraithTimer = 60 * 20;

	public EntityTimeRemnant(World world) {
		super(world);
		this.setSize(0.6F, 1.8F);
	}

	public EntityTimeRemnant(World worldIn, EntityPlayer owner) {
		this(worldIn);
		this.setPosition(owner.posX, owner.posY, owner.posZ);
		this.setCustomNameTag(owner.getDisplayNameString());
		this.setAlwaysRenderNameTag(true);

		if (!owner.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
			this.setItemStackToSlot(EntityEquipmentSlot.HEAD, owner.getItemStackFromSlot(EntityEquipmentSlot.HEAD).copy());
		if (!owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty())
			this.setItemStackToSlot(EntityEquipmentSlot.CHEST, owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST).copy());
		if (!owner.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty())
			this.setItemStackToSlot(EntityEquipmentSlot.LEGS, owner.getItemStackFromSlot(EntityEquipmentSlot.LEGS).copy());
		if (!owner.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty())
			this.setItemStackToSlot(EntityEquipmentSlot.FEET, owner.getItemStackFromSlot(EntityEquipmentSlot.FEET).copy());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void initEntityAI() {
		this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityTimeWraith.class, 6.0F, 1.0D, 1.2D));
        this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(6, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.targetTasks.addTask(4, new EntityAITargetNonTamed(this, EntityAnimal.class, false, new Predicate<Entity>() {
			public boolean apply(@Nullable Entity p_apply_1_) {
				return p_apply_1_ instanceof EntitySheep || p_apply_1_ instanceof EntityRabbit;
			}
		}));
		this.targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntitySkeleton.class, false));

		this.setAIMoveSpeed(5);
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		if (getOwner() != null) {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(getOwner().getMaxHealth());
			this.setHealth(getOwner().getHealth());
		}
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (getOwner() != null) {
			this.setCustomNameTag(((EntityPlayer) getOwner()).getDisplayNameString());
		}

		if (wraithTimer > 0) {

			if (wraithTimer == 1) {
				if (!getEntityWorld().isRemote) {
					
					Random rand = new Random();
					
					if(rand.nextInt(10) == 0) {
						EntityBlackFlash wraith = new EntityBlackFlash(getEntityWorld());
						wraith.setPosition(posX, posY, posZ);
						if(this.getOwner() != null) {
							wraith.setAttackTarget(this.getOwner());
							if(getOwner() instanceof EntityPlayer)
								((EntityPlayer)getOwner()).addStat(SHAchievements.blackFlash);
						}

						BlockPos pos = SpeedsterHeroesUtil.getRandomPositionInNear(getEntityWorld(), getPosition(), 10);
						EntityDimensionBreach breach = new EntityDimensionBreach(getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), BreachActionTypes.SPAWN_ENTITY);
						breach.entityToSpawn = wraith;

						getEntityWorld().spawnEntity(breach);
					} else {
						EntityTimeWraith wraith = new EntityTimeWraith(getEntityWorld());
						wraith.setPosition(posX, posY, posZ);
						wraith.setAttackTarget(this);
						boolean player = rand.nextBoolean();
						if (player && getOwner() != null)
							wraith.setAttackTarget(this.getOwner());
						else
							wraith.setAttackTarget(this);
						
						if(getOwner() != null && getOwner() instanceof EntityPlayer)
							((EntityPlayer)getOwner()).addStat(SHAchievements.timeWraith);

						BlockPos pos = SpeedsterHeroesUtil.getRandomPositionInNear(getEntityWorld(), getPosition(), 10);
						EntityDimensionBreach breach = new EntityDimensionBreach(getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), BreachActionTypes.SPAWN_ENTITY);
						breach.entityToSpawn = wraith;

						getEntityWorld().spawnEntity(breach);
					}
				}
			}

			wraithTimer--;
		}
	}

	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (this.isEntityInvulnerable(source)) {
			return false;
		} else {
			Entity entity = source.getEntity();

			if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
				amount = (amount + 1.0F) / 2.0F;
			}

			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Nullable
	public UUID getOwnerId() {
		return (UUID) ((Optional) this.dataManager.get(OWNER_UNIQUE_ID)).orNull();
	}

	public void setOwnerId(@Nullable UUID p_184754_1_) {
		this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(p_184754_1_));
	}

	@Override
	public EntityAgeable createChild(EntityAgeable ageable) {
		return null;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompund) {
		super.readEntityFromNBT(tagCompund);

		this.wraithTimer = tagCompund.getInteger(SHNBTTags.wraithTimer);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		super.writeEntityToNBT(tagCompound);

		tagCompound.setInteger(SHNBTTags.wraithTimer, wraithTimer);
	}

	public boolean attackEntityAsMob(Entity entityIn) {
		float f = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		int i = 0;

		if (entityIn instanceof EntityLivingBase) {
			i += EnchantmentHelper.getKnockbackModifier(this);
		}

		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag) {
			if (i > 0) {
				entityIn.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) i * 0.5F));
				this.motionX *= 0.6D;
				this.motionZ *= 0.6D;
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0) {
				entityIn.setFire(j * 4);
			}

			this.applyEnchantments(this, entityIn);
		}

		return flag;
	}

	@Override
	protected Item getDropItem() {
		return null;
	}

	@Override
	protected void dropEquipment(boolean p_82160_1_, int p_82160_2_) {

	}

}
