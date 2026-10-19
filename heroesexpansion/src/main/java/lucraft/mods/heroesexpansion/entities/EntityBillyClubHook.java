package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.items.ItemBillyClub;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityBillyClubHook extends EntityThrownBillyClub {

    public BlockPos attachedBlock;

    public EntityBillyClubHook(World worldIn) {
        super(worldIn);
    }

    public EntityBillyClubHook(World world, EntityLivingBase entity, ItemStack stack) {
        super(world, entity, stack);
    }

    @Override
    public void onBillyClubUpdate() {
        if (getThrower() == null || getThrower().isDead || this.isDead) {
            dropItem();
            return;
        }

        if (ticksExisted > 20 * 10 || this.posY < 0 || (getThrower() != null && (getDistance(this.getThrower()) >= 50) || !(getThrower().getHeldItemMainhand().getItem() instanceof ItemBillyClub) || !((ItemBillyClub) getThrower().getHeldItemMainhand().getItem()).separate)) {
            backToUser = true;
        }

        if (this.onGround)
            this.motionX = this.motionY = this.motionZ = 0D;

        if (this.attachedBlock != null && !this.world.isBlockFullCube(this.attachedBlock)) {
            backToUser = true;
            this.onGround = false;
        }

        if (backToUser) {
            this.motionX = (this.getThrower().posX - posX) / 3D;
            this.motionY = (this.getThrower().posY + (((EntityPlayer) getThrower()).height / 2D) - posY) / 3D;
            this.motionZ = (this.getThrower().posZ - posZ) / 3D;

            if (!this.world.isRemote && this.getDistance(getThrower()) <= 1.5F) {
                giveItemBack((EntityPlayer) getThrower(), getItem());
                PlayerHelper.playSoundToAll(getEntityWorld(), posX, posY, posZ, 30, getPickupSound(), SoundCategory.AMBIENT);
                this.setDead();
            }
        }

        if (this.onGround && !this.world.isRemote && !backToUser && !isDead) {
            getThrower().motionX += (this.posX - getThrower().posX) / 50F;
            getThrower().motionY += (this.posY - getThrower().posY) / 50F;
            getThrower().motionZ += (this.posZ - getThrower().posZ) / 50F;
            getThrower().fallDistance = 0F;

            ((EntityPlayerMP) getThrower()).connection.sendPacket(new SPacketEntityVelocity(getThrower()));

            if (this.getDistance(getThrower()) <= 1.5F) {
                giveItemBack((EntityPlayer) getThrower(), getItem());
                PlayerHelper.playSoundToAll(getEntityWorld(), posX, posY, posZ, 30, getPickupSound(), SoundCategory.AMBIENT);
                this.setDead();
            }
        }
    }

    @Override
    public boolean canUsePrecision() {
        return false;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!onGround && !this.world.isRemote && getThrower() != null && result.typeOfHit == RayTraceResult.Type.BLOCK && this.world.isBlockFullCube(result.getBlockPos())) {
            this.attachedBlock = result.getBlockPos();
            this.motionX = this.motionY = this.motionZ = 0F;
            this.setPositionAndUpdate(result.hitVec.x, result.hitVec.y, result.hitVec.z);
            this.onGround = true;
            return;
        }
        super.onImpact(result);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.attachedBlock != null)
            compound.setTag("AttachedBlock", NBTUtil.createPosTag(this.attachedBlock));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("AttachedBlock"))
            this.attachedBlock = NBTUtil.getPosFromTag(compound.getCompoundTag("AttachedBlock"));
    }

}
