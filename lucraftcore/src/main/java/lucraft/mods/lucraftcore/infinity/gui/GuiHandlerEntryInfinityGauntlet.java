package lucraft.mods.lucraftcore.infinity.gui;

import lucraft.mods.lucraftcore.infinity.container.ContainerInfinityGauntlet;
import lucraft.mods.lucraftcore.util.gui.LCGuiHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandlerEntryInfinityGauntlet extends LCGuiHandler.GuiHandlerEntry {

    public static final int ID = 50;

    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getClientGui(EntityPlayer player, World world, int x, int y, int z) {
        return new GuiInfinityGauntlet(player, player.getHeldItemMainhand());
    }

    @Override
    public Container getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerInfinityGauntlet(player, player.getHeldItemMainhand());
    }
}
