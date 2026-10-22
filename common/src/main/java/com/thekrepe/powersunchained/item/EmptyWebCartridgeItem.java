package com.thekrepe.powersunchained.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A used-up web cartridge ("Cartridge", grey "Empty" line); refilled with slime and string. */
public class EmptyWebCartridgeItem extends Item {

    public EmptyWebCartridgeItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.powers_unchained.cartridge_empty").withStyle(ChatFormatting.GRAY));
    }
}
