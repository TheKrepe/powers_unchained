package lucraft.mods.heroes.speedsterheroes.entity;

import lucraft.mods.heroes.speedsterheroes.client.sounds.SHSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityBlackFlash extends EntityMob {

	public EntityBlackFlash(World worldIn) {
		super(worldIn);
		this.setSize(0.6F, 1.8F);

		this.setAlwaysRenderNameTag(false);
		this.isImmuneToFire = true;

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityTimeRemnant.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		// this.tasks.addTask(4, new EntityAIAttackOnCollide(this,
		// EntityTimeRemnant.class, 1.0D, true));
//		 this.tasks.addTask(4, new EntityAIAttackOnCollide(this,
//		 EntityTimeRemnant.class, 1.0D, true));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
		this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityTimeRemnant>(this, EntityTimeRemnant.class, false));
	}

	private static final DataParameter<Float> SPAWNTIMERID = EntityDataManager.<Float> createKey(EntityBlackFlash.class, DataSerializers.FLOAT);

	@Override
	protected void entityInit() {
		super.entityInit();

		this.dataManager.register(SPAWNTIMERID, 0F);
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(25.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(5.0D);
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		return super.attackEntityAsMob(entityIn);
	}

	protected SoundEvent getAmbientSound() {
		return SHSoundEvents.blackFlashAmbient;
	}

	protected SoundEvent getHurtSound() {
		return SHSoundEvents.blackFlashHurt;
	}

	protected SoundEvent getDeathSound() {
		return SHSoundEvents.blackFlashDeath;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
		return super.canAttackClass(cls);
	}

	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	public void setDespawnTimer(int i) {
		this.dataManager.set(SPAWNTIMERID, Float.valueOf(i));
	}

	public int getDespawnTimer() {
		return Math.round(dataManager.get(SPAWNTIMERID));
	}

	@Override
	public void onLivingUpdate() {
		for (EntityTimeRemnant remnants : this.getEntityWorld().getEntitiesWithinAABB(EntityTimeRemnant.class, new AxisAlignedBB(posX - 3, posY - 3, posZ - 3, posX + 3, posY + 3, posZ + 3))) {
			remnants.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1D);
		}
		//
		// if (getAttackTarget() != null) {
		// double d = this.getDistanceSqToEntity(getAttackTarget());
		//
		// if (d > 10) {
		// this.setAIMoveSpeed(10);
		// } else {
		// this.setAIMoveSpeed(1F);
		// }
		//
		// if (d < 1)
		// attackEntityAsMob(getAttackTarget());
		// }

		// if (!this.worldObj.isRemote && getAttackTarget() != null) {
		// Entity entity = getAttackTarget();
		//
		// if (entity != null) {
		// this.motionX = (entity.posX - posX) * 0.01F;
		// this.motionY = entity.posY - posY;
		// this.motionZ = (entity.posZ - posZ) * 0.01F;
		// }
		// }

		if (getAttackTarget() != null && getAttackTarget() instanceof EntityTimeRemnant) {
			EntityTimeRemnant remnant = (EntityTimeRemnant) getAttackTarget();

			if (remnant.getOwner() != null && remnant.getOwner() instanceof EntityPlayer) {
				this.setAttackTarget(remnant.getOwner());
			}
		}

		if (getDespawnTimer() > 0) {
			setDespawnTimer(getDespawnTimer() + 1);

			if (getDespawnTimer() == 5 * 20)
				this.setDead();
		}

		super.onLivingUpdate();
	}

	public boolean isOnLadder() {
		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		super.writeEntityToNBT(tagCompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompund) {
		super.readFromNBT(tagCompund);
	}

	public void fall(float distance, float damageMultiplier) {
	}

	protected void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos) {
	}

	@Override
	public boolean getAlwaysRenderNameTag() {
		return false;
	}

	@Override
	public boolean getAlwaysRenderNameTagForRender() {
		return false;
	}

}
