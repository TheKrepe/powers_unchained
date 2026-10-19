package lucraft.mods.lucraftcore.extendedinventory.gui;

import lucraft.mods.lucraftcore.extendedinventory.container.ContainerExtendedInventory;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler.GuiHandlerEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandlerEntryExtendedInventory extends GuiHandlerEntry {

    public static final int ID = 0;

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getClientGui(EntityPlayer player, World world, int x, int y, int z) {
        return new GuiExtendedInventory(player);
    }

    @Override
    public Container getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerExtendedInventory(player);
    }

}
