package lucraft.mods.heroes.speedsterheroes.tileentities;

import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class TileEntitySpeedforceDampener extends TileEntity implements ITickable {

	public int timer;
	
	@Override
	public void update() {
		
		if(timer > 0) {
			timer++;
			
			if(timer == 50)
				timer = 0;
		}
		
		if(!this.getWorld().isBlockPowered(pos)) {
			
			if(timer == 0)
				timer = 1;
			
			for(EntityPlayer player : this.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(new BlockPos(this.getPos().getX() - 2, this.getPos().getY() - 2, this.getPos().getZ() - 2), new BlockPos(this.getPos().getX() + 3, this.getPos().getY() + 3, this.getPos().getZ() + 3)))) {
				if(SpeedsterHeroesUtil.isEntityAvailableForSpeedforce(player)) {
					SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
					
					if(data != null && data.isInSpeed) {
						data.toggleSpeed();
					}
				}
				
			}
		}
		
	}
	
	
	
}
