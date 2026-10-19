package lucraft.mods.lucraftcore.superpowers.toasts;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class SuperpowerLevelUpToast implements IToast {

    public SuperpowerLevelUpToast() {

    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        EntityPlayer player = toastGui.getMinecraft().player;
        Superpower superpower = SuperpowerHandler.getSuperpower(player);
        AbilityContainer container = Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, player);
        AbilityContainerSuperpower handler = container instanceof AbilityContainerSuperpower ? (AbilityContainerSuperpower) container : null;

        if (handler == null || superpower == null)
            return Visibility.HIDE;

        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);
        GlStateManager.pushMatrix();
        GlStateManager.translate(8, 8, 0);
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        superpower.renderIcon(toastGui.getMinecraft(), toastGui, 0, 0);
        GlStateManager.popMatrix();

        String s = StringHelper.translateToLocal("lucraftcore.info.levelup").replace("%s", TextFormatting.GOLD + "" + handler.getLevel() + TextFormatting.RESET);
        List<String> list = toastGui.getMinecraft().fontRenderer.listFormattedStringToWidth(s, 125);

        for (int i = 0; i < list.size(); i++) {
            toastGui.getMinecraft().fontRenderer.drawString(list.get(i), 30, 7 + i * 11, -1);
        }

        return delta >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }

}
