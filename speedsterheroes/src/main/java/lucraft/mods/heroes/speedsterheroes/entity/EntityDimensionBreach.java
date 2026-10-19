package lucraft.mods.heroes.speedsterheroes.entity;

import io.netty.buffer.ByteBuf;
import lucraft.mods.heroes.speedsterheroes.util.SHNBTTags;
import lucraft.mods.heroes.speedsterheroes.util.TeleportDestination;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityDimensionBreach extends Entity implements IEntityAdditionalSpawnData {

	public BreachActionTypes type;
	public TeleportDestination teleportDestination = TeleportDestination.DEFAULT;
	public int progress = 20;
	public boolean starting;
	public boolean despawn;

	public Entity entityToSpawn;

	public EntityDimensionBreach(World world) {
		super(world);
		this.setSize(3, 3);
	}

	public EntityDimensionBreach(World worldIn, double x, double y, double z, BreachActionTypes type) {
		super(worldIn);
		this.setPosition(x, y, z);
		this.type = type;
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		if (this.ticksExisted < 20 && !despawn) {
			progress--;
			starting = true;
		}
		
		if(despawn) {
			progress++;
			starting = false;
			if (progress == 20)
				this.setDead();
		}
		
		// SPAWN ENTITY
		// ----------------------------------------------------------------------
		if(type == BreachActionTypes.SPAWN_ENTITY) {
			if (this.ticksExisted > 4 * 20) {
				this.startDespawn();
			}
			
			if(ticksExisted == 25 && !this.getEntityWorld().isRemote && entityToSpawn != null) {
				entityToSpawn.setPosition(posX, posY, posZ);
				getEntityWorld().spawnEntity(entityToSpawn);
			}
		}
		
		if(type == BreachActionTypes.PORTAL) {
			if(this.getControllingPassenger() == null && ticksExisted > 10*20) {
				this.startDespawn();
			}
		}
//		for(WorldServer servers : DimensionManager.getWorlds()) {
//			System.out.println("" + servers.provider.getDimensionName());
//		}
		// -----------------------------------------------------------------------------------
	}

	public void startDespawn() {
		this.despawn = true;
	}
	
	@Override
	protected void entityInit() {

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		this.progress = tagCompund.getInteger(SHNBTTags.progress);
		if(tagCompund.getInteger(SHNBTTags.type) != -1)
			this.type = BreachActionTypes.getTypeFromId(tagCompund.getInteger(SHNBTTags.type));
		this.teleportDestination = TeleportDestination.fromNBT(tagCompund.getCompoundTag("TeleportDestination"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		tagCompound.setInteger(SHNBTTags.progress, progress);
		tagCompound.setInteger(SHNBTTags.type, type == null ? -1 : type.ordinal());
		tagCompound.setTag("TeleportDestination", teleportDestination.serializeNBT());
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer entityIn) {
		super.onCollideWithPlayer(entityIn);
		
		if(!isPassenger(entityIn) && this.type == BreachActionTypes.PORTAL && entityIn instanceof EntityPlayerMP && !despawn) {
			LucraftCoreUtil.teleportToDimension(entityIn, teleportDestination.getDimensionId(), teleportDestination.getX(), teleportDestination.getY(), teleportDestination.getZ());
		}
	}
	
	public enum BreachActionTypes {

		NONE, SPAWN_ENTITY, PORTAL;

		public static BreachActionTypes getTypeFromId(int id) {
			for (BreachActionTypes types : BreachActionTypes.values()) {
				if (types.ordinal() == id) {
					return types;
				}
			}
			return null;
		}

	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeEntityToNBT(nbt);
		ByteBufUtils.writeTag(buffer, nbt);
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		this.readEntityFromNBT(ByteBufUtils.readTag(additionalData));
	}

}
