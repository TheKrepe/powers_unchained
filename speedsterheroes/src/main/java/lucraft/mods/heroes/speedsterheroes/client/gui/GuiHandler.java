package lucraft.mods.heroes.speedsterheroes.client.gui;

import lucraft.mods.lucraftcore.container.ContainerDummy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity tileEntity = world.getTileEntity(pos);

		switch (ID) {
		case GuiIds.dimensionBreach:
			return new GuiDimensionBreach(player);
		case GuiIds.newWaypoint:
			return new GuiNewWaypoint();
		default:
			return null;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity tileEntity = world.getTileEntity(pos);
		switch (ID) {
			case GuiIds.dimensionBreach:
		default:
			return new ContainerDummy();
		}
	}

}
