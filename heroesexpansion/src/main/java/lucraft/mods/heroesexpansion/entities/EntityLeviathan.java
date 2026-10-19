package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class EntityLeviathan extends EntityFlying {

    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
    public UUID portal;

    public EntityLeviathan(World worldIn) {
        super(worldIn);
        this.setSize(8.0F, 4.0F);
        this.setHealth(this.getMaxHealth());
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
        this.moveHelper = new EntityLeviathan.LeviathanMoveHelper(this);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(100.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(7, new EntityLeviathan.AILookAround(this));
        this.tasks.addTask(5, new EntityLeviathan.AIRandomFly(this));
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return HESoundEvents.LEVIATHAN_AMBIENT;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (!this.world.isRemote && this.portal != null && this.ticksExisted % 100 == 0) {
            Entity entity = LCEntityHelper.getEntityByUUID(this.getEntityWorld(), this.portal);

            if (entity == null || entity.isDead)
                this.setHealth(0);
        }

        int minute = 20 * 10;
        if (!this.world.isRemote && this.ticksExisted % minute == 0) {
            int j = 4 - this.world.getDifficulty().getId();
            for (int i = 0; i < 8; i++) {
                if (new Random().nextInt(j) == 0) {
                    Vec3d v = this.getLookVec();
                    v = v.add(0, -v.y, 0);
                    Vec3d pos = this.getPositionVector().add(v.scale((i - 4) * 2));
                    EntityChitauri chitauri = new EntityChitauri(this.world);
                    chitauri.setPosition(pos.x, pos.y, pos.z);
                    chitauri.rotationYaw = this.rotationYaw + (i % 2 == 0 ? 90 : -90);
                    chitauri.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(chitauri)), null);
                    chitauri.portal = this.portal;
                    this.world.spawnEntity(chitauri);
                    chitauri.moveRelative(0, 0, 1, 1);
                }
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith();
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0F;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if (portal != null)
            compound.setString("Portal", this.portal.toString());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (this.hasCustomName())
            this.bossInfo.setName(this.getDisplayName());

        if (compound.hasKey("Portal"))
            this.portal = UUID.fromString(compound.getString("Portal"));
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    static class AIRandomFly extends EntityAIBase {

        private final EntityLeviathan parentEntity;

        public AIRandomFly(EntityLeviathan leviathan) {
            this.parentEntity = leviathan;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();

            if (!entitymovehelper.isUpdating()) {
                return true;
            } else {
                double d0 = entitymovehelper.getX() - this.parentEntity.posX;
                double d1 = entitymovehelper.getY() - this.parentEntity.posY;
                double d2 = entitymovehelper.getZ() - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                return d3 < 1.0D || d3 > 3600.0D;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            Random random = this.parentEntity.getRNG();
            double d0 = this.parentEntity.posX + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d1 = this.parentEntity.posY + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            double d2 = this.parentEntity.posZ + (double) ((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
            this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0D);
        }
    }

    static class AILookAround extends EntityAIBase {
        private final EntityLeviathan parentEntity;

        public AILookAround(EntityLeviathan leviathan) {
            this.parentEntity = leviathan;
            this.setMutexBits(2);
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public void updateTask() {
            if (this.parentEntity.getAttackTarget() == null) {
                this.parentEntity.rotationYaw = -((float) MathHelper.atan2(this.parentEntity.motionX, this.parentEntity.motionZ)) * (180F / (float) Math.PI);
                this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
            } else {
                EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
                double d0 = 64.0D;

                if (entitylivingbase.getDistanceSq(this.parentEntity) < 4096.0D) {
                    double d1 = entitylivingbase.posX - this.parentEntity.posX;
                    double d2 = entitylivingbase.posZ - this.parentEntity.posZ;
                    this.parentEntity.rotationYaw = -((float) MathHelper.atan2(d1, d2)) * (180F / (float) Math.PI);
                    this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw;
                }
            }
        }
    }

    static class LeviathanMoveHelper extends EntityMoveHelper {
        private final EntityLeviathan parentEntity;
        private int courseChangeCooldown;

        public LeviathanMoveHelper(EntityLeviathan leviathan) {
            super(leviathan);
            this.parentEntity = leviathan;
        }

        public void onUpdateMoveHelper() {
            if (this.action == EntityMoveHelper.Action.MOVE_TO) {
                double d0 = this.posX - this.parentEntity.posX;
                double d1 = this.posY - this.parentEntity.posY;
                double d2 = this.posZ - this.parentEntity.posZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (this.courseChangeCooldown-- <= 0) {
                    this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
                    d3 = (double) MathHelper.sqrt(d3);

                    if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
                        this.parentEntity.motionX += d0 / d3 * 0.1D;
                        this.parentEntity.motionY += d1 / d3 * 0.1D;
                        this.parentEntity.motionZ += d2 / d3 * 0.1D;
                    } else {
                        this.action = EntityMoveHelper.Action.WAIT;
                    }
                }
            }
        }

        private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
            double d0 = (x - this.parentEntity.posX) / p_179926_7_;
            double d1 = (y - this.parentEntity.posY) / p_179926_7_;
            double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; (double) i < p_179926_7_; ++i) {
                axisalignedbb = axisalignedbb.offset(d0, d1, d2);

                if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
                    return false;
                }
            }

            return true;
        }
    }

}
