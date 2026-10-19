package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityBoiler;
import lucraft.mods.lucraftcore.utilities.container.ContainerBoiler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandlerEntryBoiler extends LCGuiHandler.GuiHandlerEntry {

    public static final int ID = 9;

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getClientGui(EntityPlayer player, World world, int x, int y, int z) {
        return new GuiBoiler(player, (TileEntityBoiler) world.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Container getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBoiler(player, (TileEntityBoiler) world.getTileEntity(new BlockPos(x, y, z)));
    }
}
