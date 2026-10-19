package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageKineticEnergyBlast;
import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityTesseract extends EntityItemIndestructible {

    private static final DataParameter<Integer> BREAK_TIMER = EntityDataManager.<Integer>createKey(EntityTesseract.class, DataSerializers.VARINT);

    public EntityTesseract(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
    }

    public EntityTesseract(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(BREAK_TIMER, Integer.valueOf(0));
    }

    public void setBreakTimer(int breakTimer) {
        this.dataManager.set(BREAK_TIMER, Integer.valueOf(breakTimer));
    }

    public int getBreakTimer() {
        return this.dataManager.get(BREAK_TIMER).intValue();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if (getBreakTimer() > 0) {
            this.motionY = 0.07F;
            this.setBreakTimer(getBreakTimer() + 1);
            if (getBreakTimer() == 98) {
                HEPacketDispatcher.sendToAll(new MessageKineticEnergyBlast(this.posX, this.posY, this.posZ, 3, 0, 1, 1));
                PlayerHelper.playSoundToAll(this.world, this.posX, this.posY, this.posZ, 50, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS);
            }
            if (!this.world.isRemote && getBreakTimer() >= 100) {
                this.setDead();
                this.world.spawnEntity(new EntityItemIndestructible(this.world, this.posX, this.posY, this.posZ, new ItemStack(HEItems.SPACE_STONE)));
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean hitByEntity(Entity entityIn) {
        if (this.getBreakTimer() <= 0)
            this.setBreakTimer(1);
        return super.hitByEntity(entityIn);
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (getBreakTimer() > 0)
            return EnumActionResult.FAIL;
        return super.applyPlayerInteraction(player, vec, hand);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setBreakTimer(compound.getInteger("BreakTimer"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("BreakTimer", this.getBreakTimer());
    }
}
