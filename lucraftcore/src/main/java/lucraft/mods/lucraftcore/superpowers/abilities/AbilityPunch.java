package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.util.attributes.LCAttributes;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityPunch extends AbilityAttributeModifier {

    public AbilityPunch(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 2);
    }

    @Override
    public IAttribute getAttribute() {
        return LCAttributes.PUNCH_DAMAGE;
    }

}
