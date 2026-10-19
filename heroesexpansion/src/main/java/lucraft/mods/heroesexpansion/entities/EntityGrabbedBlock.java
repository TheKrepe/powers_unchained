package lucraft.mods.heroesexpansion.entities;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.Objects;

public class EntityGrabbedBlock extends Entity implements IEntityAdditionalSpawnData {

    public IBlockState blockState;
    public NBTTagCompound tileData;
    public BlockPos origin;
    public boolean released = false;

    public EntityGrabbedBlock(World worldIn) {
        super(worldIn);
        this.setSize(1, 1);
    }

    @Override
    protected void entityInit() {

    }

    public EntityGrabbedBlock(World world, BlockPos pos) {
        this(world);
        this.origin = pos;

        if (world.isAirBlock(pos)) {
            this.setDead();
            return;
        }

        this.blockState = world.getBlockState(pos);
        if (world.getTileEntity(pos) != null) {
            this.tileData = world.getTileEntity(pos).serializeNBT();
            world.removeTileEntity(pos);
        }

        world.setBlockToAir(pos);

        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;

        float f1 = this.isInWater() ? 0.8F : 0.99F;
        this.motionX *= (double) f1;
        this.motionY *= (double) f1;
        this.motionZ *= (double) f1;

        this.setPosition(this.posX, this.posY, this.posZ);

        if (this.released) {
            tryPlace();
        }
    }

    public boolean tryPlace() {
        if (!this.world.isAirBlock(this.getPosition()) && !this.isDead && !this.world.isRemote) {
            BlockPos pos = new BlockPos(prevPosX, prevPosY, prevPosZ);

            if(place(pos)) {
                this.setDead();
                return true;
            }

            BlockPos up = pos.up();
            if (place(up)) {
                this.setDead();
                return true;
            }

            for (EnumFacing facing : EnumFacing.VALUES) {
                if (place(pos.add(facing.getDirectionVec()))) {
                    this.setDead();
                    return true;
                }
            }

            blockState.getBlock().dropBlockAsItem(this.world, pos, blockState, 0);
        }

        return false;
    }

    protected boolean place(BlockPos pos) {
        if (this.released && !this.isDead) {
            if (this.world.isAirBlock(pos)) {
                this.world.setBlockState(pos, this.blockState);

                if (this.tileData != null && blockState.getBlock().hasTileEntity(this.blockState)) {
                    TileEntity tileentity = this.world.getTileEntity(pos);

                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());

                        for (String s : this.tileData.getKeySet()) {
                            NBTBase nbtbase = this.tileData.getTag(s);

                            if (!"x".equals(s) && !"y".equals(s) && !"z".equals(s)) {
                                nbttagcompound.setTag(s, nbtbase.copy());
                            }
                        }

                        tileentity.readFromNBT(nbttagcompound);
                        tileentity.markDirty();
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        int i = compound.getByte("Data") & 255;
        if (compound.hasKey("TileEntityData", 10))
            this.tileData = compound.getCompoundTag("TileEntityData");
        if (compound.hasKey("Block", 8))
            this.blockState = Block.getBlockFromName(compound.getString("Block")).getStateFromMeta(i);
        else if (compound.hasKey("TileID", 99))
            this.blockState = Block.getBlockById(compound.getInteger("TileID")).getStateFromMeta(i);
        else
            this.blockState = Block.getBlockById(compound.getByte("Tile") & 255).getStateFromMeta(i);

        if (compound.hasKey("TileEntityData", 10))
            this.tileData = compound.getCompoundTag("TileEntityData");

        this.origin = NBTUtil.getPosFromTag(compound.getCompoundTag("Origin"));

        this.released = compound.getBoolean("Released");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        Block block = this.blockState != null ? this.blockState.getBlock() : Blocks.AIR;
        ResourceLocation resourcelocation = Block.REGISTRY.getNameForObject(block);
        compound.setString("Block", resourcelocation == null ? "" : resourcelocation.toString());
        compound.setByte("Data", (byte) block.getMetaFromState(this.blockState));

        if (this.tileData != null)
            compound.setTag("TileEntityData", this.tileData);

        compound.setTag("Origin", NBTUtil.createPosTag(this.origin));

        compound.setBoolean("Released", this.released);
    }

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        NBTTagCompound nbt = new NBTTagCompound();
        writeEntityToNBT(nbt);
        ByteBufUtils.writeTag(buffer, nbt);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData) {
        readEntityFromNBT(Objects.requireNonNull(ByteBufUtils.readTag(additionalData)));
    }
}