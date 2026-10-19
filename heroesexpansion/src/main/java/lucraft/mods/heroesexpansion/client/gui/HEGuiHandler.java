package lucraft.mods.heroesexpansion.client.gui;

import lucraft.mods.heroesexpansion.container.ContainerPortalDevice;
import lucraft.mods.heroesexpansion.container.ContainerQuiver;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class HEGuiHandler implements IGuiHandler {

    public static final int QUIVER = 0;
    public static final int PORTAL_DEVICE = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == QUIVER)
            return new ContainerQuiver(player, player.getHeldItemMainhand());
        if (ID == PORTAL_DEVICE)
            return new ContainerPortalDevice(player.inventory, (TileEntityPortalDevice) world.getTileEntity(new BlockPos(x, y, z)));

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == QUIVER)
            return new GuiQuiver(player, player.getHeldItemMainhand());
        if (ID == PORTAL_DEVICE)
            return new GuiPortalDevice(player.inventory, (TileEntityPortalDevice) world.getTileEntity(new BlockPos(x, y, z)));

        return null;
    }

}
