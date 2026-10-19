package lucraft.mods.lucraftcore.infinity;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.items.ItemIndestructible;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public class EntityItemIndestructible extends Entity {

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.<ItemStack>createKey(EntityItemIndestructible.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Float> HEIGHT = EntityDataManager.<Float>createKey(EntityItemIndestructible.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> WIDTH = EntityDataManager.<Float>createKey(EntityItemIndestructible.class, DataSerializers.FLOAT);

    public EntityItemIndestructible(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.setItem(stack);
        this.rotationYaw = (float) (Math.random() * 360.0D);
    }

    public EntityItemIndestructible(World worldIn, double x, double y, double z, ItemStack stack, float height, float width) {
        this(worldIn);
        this.setEntitySize(height, width);
        this.setPosition(x, y, z);
        this.setItem(stack);
        this.rotationYaw = (float) (Math.random() * 360.0D);
    }

    public EntityItemIndestructible(World worldIn) {
        super(worldIn);
        this.setSize(getWidth(), getHeight());
        this.isImmuneToFire = true;
    }

    public void setEntitySize(float height, float width) {
        this.setHeight(height);
        this.setWidth(width);
        this.setSize(width, height);
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(ITEM, ItemStack.EMPTY);
        this.getDataManager().register(HEIGHT, 0.25F);
        this.getDataManager().register(WIDTH, 0.25F);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
        this.setItem(new ItemStack(nbttagcompound));

        if (this.getItem().isEmpty())
            this.setDead();

        this.setHeight(compound.getFloat("Height"));
        this.setWidth(compound.getFloat("Width"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (!this.getItem().isEmpty())
            compound.setTag("Item", this.getItem().writeToNBT(new NBTTagCompound()));

        compound.setFloat("Height", getHeight());
        compound.setFloat("Width", getWidth());
    }

    public ItemStack getItem() {
        return (ItemStack) this.getDataManager().get(ITEM);
    }

    public void setItem(ItemStack stack) {
        this.getDataManager().set(ITEM, stack);
        this.getDataManager().setDirty(ITEM);
    }

    public float getHeight() {
        return this.getDataManager().get(HEIGHT);
    }

    public void setHeight(float height) {
        this.getDataManager().set(HEIGHT, height);
    }

    public float getWidth() {
        return this.getDataManager().get(WIDTH);
    }

    public void setWidth(float width) {
        this.getDataManager().set(WIDTH, width);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    protected void dealFireDamage(int amount) {
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (this.isDead || (this.getItem().getItem() instanceof ItemIndestructible && ((ItemIndestructible) this.getItem().getItem()).onItemPickUp(this.world, player, this.getItem(), this)))
            return EnumActionResult.FAIL;

        PlayerHelper.givePlayerItemStack(player, this.getItem());
        this.setDead();
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        double d0 = this.motionX;
        double d1 = this.motionY;
        double d2 = this.motionZ;

        this.setSize(getWidth(), getHeight());

        if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
        }

        if (this.world.isRemote) {
            this.noClip = false;
        } else {
            this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        boolean flag = (int) this.prevPosX != (int) this.posX || (int) this.prevPosY != (int) this.posY || (int) this.prevPosZ != (int) this.posZ;

        if (flag || this.ticksExisted % 25 == 0) {
            if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
                this.motionY = 0.20000000298023224D;
                this.motionX = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.motionZ = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
            }
        }

        float f = 0.98F;

        if (this.onGround) {
            BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
            net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
            f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
        }

        this.motionX *= (double) f;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= (double) f;

        if (this.onGround) {
            this.motionY *= -0.5D;
        }

        this.handleWaterMovement();

        if (!this.world.isRemote) {
            double d3 = this.motionX - d0;
            double d4 = this.motionY - d1;
            double d5 = this.motionZ - d2;
            double d6 = d3 * d3 + d4 * d4 + d5 * d5;

            if (d6 > 0.01D) {
                this.isAirBorne = true;
            }
        }

        if (this.posY <= 0) {
            this.motionY = 0;
        }
    }
}
