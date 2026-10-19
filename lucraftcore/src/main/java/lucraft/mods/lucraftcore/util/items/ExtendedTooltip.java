package lucraft.mods.lucraftcore.util.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.util.List;

@EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
public class ExtendedTooltip {

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent e) {
        if (e.getItemStack().getItem() instanceof IExtendedItemToolTip) {
            IExtendedItemToolTip item = (IExtendedItemToolTip) e.getItemStack().getItem();
            ItemStack stack = e.getItemStack();
            EntityPlayer player = e.getEntityPlayer();

            if (player == null)
                return;

            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

            if (shift && item.shouldShiftTooltipAppear(stack, player))
                e.getToolTip().addAll(item.getShiftToolTip(stack, player));
            else if (ctrl && item.shouldCtrlTooltipAppear(stack, player)) {
                e.getToolTip().addAll(item.getCtrlToolTip(stack, player));
            } else {
                if (item.shouldShiftTooltipAppear(stack, player))
                    e.getToolTip().add(TextFormatting.GRAY + StringHelper.translateToLocal("lucraftcore.info.shifttooltip").replace("%KEY", TextFormatting.YELLOW + "SHIFT" + TextFormatting.GRAY));
                if (item.shouldCtrlTooltipAppear(stack, player) && e.getItemStack().getItem() instanceof ItemSuitSetArmor && ((ItemSuitSetArmor) e.getItemStack().getItem()).getSuitSet().addDefaultAbilities(player, new Ability.AbilityMap(), Ability.EnumAbilityContext.SUIT).size() > 0)
                    e.getToolTip().add(TextFormatting.GRAY + StringHelper.translateToLocal("lucraftcore.info.controltooltip").replace("%KEY", TextFormatting.GOLD + "CONTROL" + TextFormatting.GRAY));
            }
        }

        // int[] ia = OreDictionary.getOreIDs(e.getItemStack());
        //
        // for(int i : ia) {
        // e.getToolTip().add(OreDictionary.getOreNames()[i]);
        // }
    }

    public interface IExtendedItemToolTip {

        boolean shouldShiftTooltipAppear(ItemStack stack, EntityPlayer player);

        List<String> getShiftToolTip(ItemStack stack, EntityPlayer player);

        boolean shouldCtrlTooltipAppear(ItemStack stack, EntityPlayer player);

        List<String> getCtrlToolTip(ItemStack stack, EntityPlayer player);

    }

}
