package lucraft.mods.lucraftcore.utilities.gui;

import lucraft.mods.lucraftcore.util.gui.LCGuiHandler.GuiHandlerEntry;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntityExtractor;
import lucraft.mods.lucraftcore.utilities.container.ContainerExtractor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandlerEntryExtractor extends GuiHandlerEntry {

    public static final int ID = 7;

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getClientGui(EntityPlayer player, World world, int x, int y, int z) {
        return new GuiExtractor(player, (TileEntityExtractor) world.getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Container getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerExtractor(player, (TileEntityExtractor) world.getTileEntity(new BlockPos(x, y, z)));
    }

}
