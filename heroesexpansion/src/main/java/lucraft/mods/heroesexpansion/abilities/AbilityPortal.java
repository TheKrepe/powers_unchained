package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageSendInfoToClient;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPortal extends AbilityAction {

    public AbilityPortal(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 4);
    }

    @Override
    public boolean action() {
        if (this.entity instanceof EntityPlayerMP)
            HEPacketDispatcher.sendTo(new MessageSendInfoToClient(MessageSendInfoToClient.ClientMessageType.PORTAL_GUI), (EntityPlayerMP) this.entity);
        return true;
    }
}
