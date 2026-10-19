package lucraft.mods.lucraftcore.util.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

public class LCGuiHandler implements IGuiHandler {

    private static HashMap<Integer, GuiHandlerEntry> guis = new HashMap<>();

    public static void registerGuiHandlerEntry(int id, GuiHandlerEntry entry) {
        if (guis.containsKey(id))
            LucraftCore.LOGGER.error("That gui handler entry id is already taken!");
        else {
            guis.put(id, entry);
        }
    }

    public static void openGui(EntityPlayer player, int id) {
        if (!guis.containsKey(id))
            LucraftCore.LOGGER.error("There is no gui registered with the id '" + id + "'!");
        else
            player.openGui(LucraftCore.INSTANCE, id, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (guis.containsKey(ID))
            return guis.get(ID).getServerContainer(player, world, x, y, z);

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (guis.containsKey(ID))
            return guis.get(ID).getClientGui(player, world, x, y, z);

        return null;
    }

    public static abstract class GuiHandlerEntry {

        @SideOnly(Side.CLIENT)
        public abstract GuiScreen getClientGui(EntityPlayer player, World world, int x, int y, int z);

        public abstract Container getServerContainer(EntityPlayer player, World world, int x, int y, int z);

    }

}
