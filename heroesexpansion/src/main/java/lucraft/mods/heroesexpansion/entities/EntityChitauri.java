package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.HELootTableList;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.lucraftcore.superpowers.entities.EntityEnergyBlast;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class EntityChitauri extends EntityMob implements IRangedAttackMob {

    public UUID portal;
    private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.<Boolean>createKey(EntityChitauri.class, DataSerializers.BOOLEAN);
    private final EntityAIAttackChitauriGun<EntityChitauri> aiArrowAttack = new EntityAIAttackChitauriGun<EntityChitauri>(this, 1.0D, 100, 15.0F);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAIAttackMelee(this, 1.2D, false) {
        @Override
        public void resetTask() {
            super.resetTask();
            EntityChitauri.this.setSwingingArms(false);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            EntityChitauri.this.setSwingingArms(true);
        }
    };

    public EntityChitauri(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.99F);
        this.setCombatTask();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SWINGING_ARMS, Boolean.valueOf(false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return HELootTableList.CHITAURI_LOOT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return HESoundEvents.CHITAURI_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return HESoundEvents.CHITAURI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HESoundEvents.CHITAURI_DEATH;
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
        super.dropLoot(wasRecentlyHit, lootingModifier, source);
        this.dropEquipment(wasRecentlyHit, lootingModifier);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.fallDistance = 0;
        if (!this.world.isRemote && this.portal != null && this.ticksExisted % 100 == 0) {
            Entity entity = LCEntityHelper.getEntityByUUID(this.getEntityWorld(), this.portal);

            if (entity == null || entity.isDead)
                this.setHealth(0);
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        return livingdata;
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(HEItems.CHITAURI_GUN));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setCombatTask();

        if (compound.hasKey("Portal"))
            this.portal = UUID.fromString(compound.getString("Portal"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if (portal != null)
            compound.setString("Portal", this.portal.toString());
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
        super.setItemStackToSlot(slotIn, stack);

        if (!this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityEnergyBlast energyBlast = new EntityEnergyBlast(this.world, this, 5, new Color(0f, 0f, 1f));
        energyBlast.shoot(this, (float) this.rotationPitch, (float) this.rotationYaw, 0.0F, 1.5F, 1.0F);
        this.playSound(LCSoundEvents.ENERGY_BLAST, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(energyBlast);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        this.dataManager.set(SWINGING_ARMS, Boolean.valueOf(swingingArms));
    }

    @SideOnly(Side.CLIENT)
    public boolean isSwingingArms() {
        return ((Boolean) this.dataManager.get(SWINGING_ARMS)).booleanValue();
    }

    public void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() == HEItems.CHITAURI_GUN) {
                int i = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                    i = 40;
                }

                this.aiArrowAttack.setAttackCooldown(i);
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }

    @Override
    public void setActiveHand(EnumHand hand) {
        ItemStack itemstack = this.getHeldItem(hand);

        if (!itemstack.isEmpty() && !this.isHandActive()) {
            int duration = net.minecraftforge.event.ForgeEventFactory.onItemUseStart(this, itemstack, itemstack.getMaxItemUseDuration());

            if (duration <= 0)
                return;
            this.activeItemStack = itemstack;
            this.activeItemStackUseCount = duration;

            if (!this.world.isRemote) {
                int i = 1;

                if (hand == EnumHand.OFF_HAND) {
                    i |= 2;
                }

                this.dataManager.set(HAND_STATES, Byte.valueOf((byte) i));
            }
        }
    }

    public static class EntityAIAttackChitauriGun<T extends EntityMob & IRangedAttackMob> extends EntityAIBase {

        private final T entity;
        private final double moveSpeedAmp;
        private int attackCooldown;
        private final float maxAttackDistance;
        private int attackTime = -1;
        private int seeTime;
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        public EntityAIAttackChitauriGun(T entity, double moveSpeedAmp, int attackCooldown, float maxAttackDistance) {
            this.entity = entity;
            this.moveSpeedAmp = moveSpeedAmp;
            this.attackCooldown = attackCooldown;
            this.maxAttackDistance = maxAttackDistance * maxAttackDistance;
            this.setMutexBits(3);
        }

        public void setAttackCooldown(int attackCooldown) {
            this.attackCooldown = attackCooldown;
        }

        @Override
        public boolean shouldExecute() {
            return this.entity.getAttackTarget() == null ? false : this.isGunInMainhand();
        }

        protected boolean isGunInMainhand() {
            return !this.entity.getHeldItemMainhand().isEmpty() && this.entity.getHeldItemMainhand().getItem() == HEItems.CHITAURI_GUN;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return (this.shouldExecute() || !this.entity.getNavigator().noPath()) && this.isGunInMainhand();
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            ((IRangedAttackMob) this.entity).setSwingingArms(true);
        }

        @Override
        public void resetTask() {
            super.resetTask();
            ((IRangedAttackMob) this.entity).setSwingingArms(false);
            this.seeTime = 0;
            this.attackTime = -1;
            this.entity.resetActiveHand();
        }

        @Override
        public void updateTask() {
            EntityLivingBase entitylivingbase = this.entity.getAttackTarget();

            if (entitylivingbase != null) {
                double d0 = this.entity.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY, entitylivingbase.posZ);
                boolean flag = this.entity.getEntitySenses().canSee(entitylivingbase);
                boolean flag1 = this.seeTime > 0;

                if (flag != flag1) {
                    this.seeTime = 0;
                }

                if (flag) {
                    ++this.seeTime;
                } else {
                    --this.seeTime;
                }

                if (d0 <= (double) this.maxAttackDistance && this.seeTime >= 20) {
                    this.entity.getNavigator().clearPath();
                    ++this.strafingTime;
                } else {
                    this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.moveSpeedAmp);
                    this.strafingTime = -1;
                }

                if (this.strafingTime >= 20) {
                    if ((double) this.entity.getRNG().nextFloat() < 0.3D) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }

                    if ((double) this.entity.getRNG().nextFloat() < 0.3D) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }

                    this.strafingTime = 0;
                }

                if (this.strafingTime > -1) {
                    if (d0 > (double) (this.maxAttackDistance * 0.75F)) {
                        this.strafingBackwards = false;
                    } else if (d0 < (double) (this.maxAttackDistance * 0.25F)) {
                        this.strafingBackwards = true;
                    }

                    this.entity.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                    this.entity.faceEntity(entitylivingbase, 30, 30);
                } else {
                    this.entity.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30F, 30F);
                }

                if (--this.attackTime <= 0 && this.seeTime >= -60) {
                    ((IRangedAttackMob) this.entity).attackEntityWithRangedAttack(entitylivingbase, 0);
                    this.attackTime = this.attackCooldown;
                }

//				if (this.entity.isHandActive()) {
//					if (!flag && this.seeTime < -60) {
//						this.entity.resetActiveHand();
//					} else if (flag) {
//						int i = this.entity.getItemInUseMaxCount();
//
//						if (i >= 20) {
//							this.entity.resetActiveHand();
//							((IRangedAttackMob) this.entity).attackEntityWithRangedAttack(entitylivingbase, 0);
//							this.attackTime = this.attackCooldown;
//						}
//					}
//				} else if (--this.attackTime <= 0 && this.seeTime >= -60) {
//					this.entity.setActiveHand(EnumHand.MAIN_HAND);
//				}
            }
        }
    }

}
