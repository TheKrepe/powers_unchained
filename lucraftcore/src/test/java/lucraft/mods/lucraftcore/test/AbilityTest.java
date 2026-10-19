package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;

public class AbilityTest extends AbilityAction {

    public int i;

    public AbilityTest(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        gui.drawString(mc.fontRenderer, "" + i, x, y, 0xffffff);
    }

    @Override
    public boolean action() {
        i++;
        if (i > 5)
            i = 0;

        if (entity instanceof EntityPlayer)
            ((EntityPlayer) entity).sendStatusMessage(new TextComponentString("" + i), true);
        this.sync = sync.add(EnumSync.SELF);
        this.markDirty();

        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setInteger("Hallo", this.i);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.i = nbt.getInteger("Hallo");
        super.deserializeNBT(nbt);
    }
}
