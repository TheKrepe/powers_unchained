package lucraft.mods.heroes.speedsterheroes.tileentities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.blocks.BlockParticleAccelerator;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.entity.EntityParticleAcceleratorSit;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntityParticleAccelerator extends TileEntity implements ITickable {

	public boolean isEnabled;
	public EntityParticleAcceleratorSit sitableEntity;
	
	public int progress;
	public int waterBuckets;
	public static final int maxProgress = 7 * 20 * 3;
	public static final int maxWaterBuckets = 7;
	
	public TileEntityParticleAccelerator() {
		this.isEnabled = false;
		this.progress = 0;
		this.waterBuckets = 0;
	}

	public void setIsEnabledAndUpdate(boolean enabled) {
		this.isEnabled = enabled;
		this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
		
		if(!enabled) {
			progress = 0;
			waterBuckets = 0;
		}
	}
	
	public boolean addWater() {
		if(waterBuckets < maxWaterBuckets) {
			this.waterBuckets++;
			this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
			return true;
		}
		return false;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(isEnabled && getWorld().getBlockState(getPos()).getBlock() instanceof BlockParticleAccelerator) {
			BlockParticleAccelerator block = (BlockParticleAccelerator) getWorld().getBlockState(getPos()).getBlock();
			return new AxisAlignedBB(block.getMinBoundingBox(getWorld(), getPos()), block.getMaxBoundingBox(getWorld(), getPos()).add(1, 1, 1));
		}
		return super.getRenderBoundingBox();
	}
	
	public void letPlayerSit(EntityPlayer player, EnumFacing facing) {
		if(player.world.isRemote)
			return;
		
		if(sitableEntity == null || sitableEntity.getEntityId() == 0 || player.world.getEntityByID(sitableEntity.getEntityId()) == null)
			sitableEntity = null;
			
		if(sitableEntity == null) {
			BlockPos sit = pos.add(facing.getOpposite().getDirectionVec());
			sitableEntity = new EntityParticleAcceleratorSit(getWorld(), sit.getX(), sit.getY(), sit.getZ(), 1.3D, false, "particleAccelerator");
			sitableEntity.facing = this.getWorld().getBlockState(getPos()).getValue(BlockParticleAccelerator.FACING);
			getWorld().spawnEntity(sitableEntity);
			player.startRiding(sitableEntity);
		}
	}
	
	public void deleteSitableEntity() {
		if(sitableEntity != null) {
			sitableEntity.setDead();
		}
	}

	@Override
	public void update() {
		if(getWorld().isBlockPowered(getPos()) && progress < maxProgress) {
			progress++;
			this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
			
		} else if(!getWorld().isBlockPowered(getPos()) && progress > 0) {
			progress -= 5;
			if(progress < 0)
				progress = 0;
			this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
			
		}
		
		if(progress == maxProgress && waterBuckets == maxWaterBuckets && isEnabled && sitableEntity != null) {
			getWorld().spawnEntity(new EntityLightningBolt(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), false));
			if(SHConfig.particleAcceleratorExplosion)
				getWorld().createExplosion(sitableEntity, getPos().getX(), getPos().getY(), getPos().getZ(), 7F, true);
			this.sitableEntity.setDead();
			progress = 0;
			waterBuckets = 0;
			this.getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
			
			
			if(sitableEntity.getPassengers().size() != 0 && sitableEntity.getPassengers().get(0) instanceof EntityPlayer)
				SuperpowerHandler.setSuperpower((EntityPlayer) sitableEntity.getPassengers().get(0), SpeedsterHeroes.speedforce);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		writeCustomToNBT(compound);
		return compound;
	}

	public void writeCustomToNBT(NBTTagCompound compound) {
		compound.setBoolean(SHNBTTags.isEnabled, isEnabled);
		compound.setInteger(SHNBTTags.progress, progress);
		compound.setInteger(SHNBTTags.waterBuckets, waterBuckets);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		readCustomFromNBT(compound);
	}

	public void readCustomFromNBT(NBTTagCompound compound) {
		this.isEnabled = compound.getBoolean(SHNBTTags.isEnabled);
		this.progress = compound.getInteger(SHNBTTags.progress);
		this.waterBuckets = compound.getInteger(SHNBTTags.waterBuckets);
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