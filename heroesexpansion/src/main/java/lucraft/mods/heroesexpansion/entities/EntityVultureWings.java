package lucraft.mods.heroesexpansion.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityVultureWings extends EntityLiving {

    public EntityVultureWings(World worldIn) {
        super(worldIn);
        this.setSize(1.5F, 1.5F);
    }

    public EntityVultureWings(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIFlying(this, 1));
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        Entity controller = getControllingPassenger();

        if (controller != null && controller instanceof EntityPlayer) {
            this.rotationYaw = ((EntityPlayer) controller).renderYawOffset;
        }
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
    }

    @Override
    public double getMountedYOffset() {
        return (double) this.height * 0.25D;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        } else {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
    }

    public class EntityAIFlying extends EntityAIBase {

        private static final float PLAYER_SPEED = 0.98f;
        private double speed;
        protected final EntityVultureWings entity;
        protected EntityPlayer rider;

        public EntityAIFlying(EntityVultureWings ant, double speed) {
            this.entity = ant;
            this.speed = speed;
        }

        @Override
        public boolean shouldExecute() {
            if (entity.getControllingPassenger() != null && entity.getControllingPassenger() instanceof EntityPlayer) {
                this.rider = (EntityPlayer) entity.getControllingPassenger();
                entity.getNavigator().clearPath();
                entity.motionX = 0;
                entity.motionZ = 0;
            } else
                this.rider = null;

            return rider != null;
        }

        @Override
        public void startExecuting() {
            entity.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
        }

        @Override
        public void updateTask() {
            super.updateTask();
            if (rider != null) {
                float speedX = rider.moveForward / PLAYER_SPEED;
                float speedY = rider.moveStrafing / PLAYER_SPEED;

                float speedPlayer = Math.max(Math.abs(speedX), Math.abs(speedY));
                Vec3d look = rider.getLookVec();
                float dir = Math.min(speedX, 0) * -1;
                dir += speedY / (speedX * 2 + (speedX < 0 ? -2 : 2));
                if (dir != 0) {
                    look = look.rotateYaw((float) Math.PI * dir);
                }
                entity.fallDistance = 0F;

                if (speedPlayer > 0F) {
                    entity.getMoveHelper().setMoveTo(entity.posX + look.x, entity.posY, entity.posZ + look.z, speed * speedPlayer);
                    entity.motionY = MathHelper.clamp((float) look.y, -0.5F, 0.5F);
                    entity.motionX = look.x * 2F;
                    entity.motionZ = look.z * 2F;
                    entity.fallDistance = 0;

                    if (!entity.shouldDismountInWater(rider) && entity.isInWater()) {
                        if (Math.abs(look.y) > 0.4) {
                            entity.motionY = Math.max(-0.15, Math.min(0.15, look.y));
                        }
                    }
                } else {
                    entity.motionY = Math.sin(entity.ticksExisted / 10F) / 100F;
                }

            }
        }
    }

}
