package lucraft.mods.heroes.speedsterheroes.entity;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.entity.EntityMountableBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class EntityParticleAcceleratorSit extends EntityMountableBlock {

	public int explosionTimer = 0;
	public static final int maxExplosionTimer = 20 * 2;
	public EnumFacing facing;
	
	public EntityParticleAcceleratorSit(World world) {
		super(world);
	}
	
	public EntityParticleAcceleratorSit(World world, double x, double y, double z, double y0ffset, boolean shouldRiderSit, String info) {
		super(world, x, y, z, y0ffset, shouldRiderSit, info);
		this.ignoreFrustumCheck = true;
	}

	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
//		if(explosionTimer > 0) {
//			if(this.riddenByEntity != null) {
//				this.riddenByEntity.mountEntity(null);
//				this.setSize(5, 5);
//			}
//			
//			explosionTimer++;
//			
//			if(explosionTimer >= maxExplosionTimer) {
//				this.setDead();
//			}
//		} else {
//			this.setDead();
//		}
		
//		explosionTimer++;
//		
//		if(explosionTimer >= maxExplosionTimer) {
//			explosionTimer = 0;
//		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);
		ByteBufUtils.writeUTF8String(buffer, facing.getName2().toLowerCase());
	}
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		super.readSpawnData(additionalData);
		facing = EnumFacing.byName(ByteBufUtils.readUTF8String(additionalData));
	}
	
}
