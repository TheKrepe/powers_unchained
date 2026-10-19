package lucraft.mods.heroes.speedsterheroes.tileentities;

import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class TileEntityParticleAcceleratorPart extends TileEntity {

	public boolean isConnected;
	public int blockPosX;
	public int blockPosY;
	public int blockPosZ;
	public boolean isSolid;
	
	public void setMasterBlock(BlockPos pos, boolean isSolid) {
		isConnected = true;
		blockPosX = pos.getX();
		blockPosY = pos.getY();
		blockPosZ = pos.getZ();
		this.isSolid = isSolid;
		this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
		this.markDirty();
	}
	
	public void disconnectFromMaster() {
		isConnected = false;
		blockPosX = 0;
		blockPosY = 0;
		blockPosZ = 0;
		isSolid = false;
		this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
	}

	public BlockPos getMasterBlock(IBlockAccess world) {
		return new BlockPos(blockPosX, blockPosY, blockPosZ);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		writeCustomToNBT(compound);
		return compound;
	}

	public void writeCustomToNBT(NBTTagCompound compound) {
		compound.setBoolean(SHNBTTags.isConnected, isConnected);
		compound.setInteger(SHNBTTags.blockPosX, blockPosX);
		compound.setInteger(SHNBTTags.blockPosY, blockPosY);
		compound.setInteger(SHNBTTags.blockPosZ, blockPosZ);
		compound.setBoolean(SHNBTTags.isSolid, isSolid);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readCustomFromNBT(compound);
	}

	public void readCustomFromNBT(NBTTagCompound compound) {
		this.isConnected = compound.getBoolean(SHNBTTags.isConnected);
		this.blockPosX = compound.getInteger(SHNBTTags.blockPosX);
		this.blockPosY = compound.getInteger(SHNBTTags.blockPosY);
		this.blockPosZ = compound.getInteger(SHNBTTags.blockPosZ);
		this.isSolid = compound.getBoolean(SHNBTTags.isSolid);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readCustomFromNBT(pkt.getNbtCompound());
		world.markBlockRangeForRenderUpdate(getPos(), getPos());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeCustomToNBT(tag);
		return new SPacketUpdateTileEntity(getPos(), 1, tag);
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = super.getUpdateTag();
		writeCustomToNBT(nbt);
		return nbt;
	}

}