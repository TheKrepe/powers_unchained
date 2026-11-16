package com.star_1mp.powersunleashed.item;

import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.registry.PUItems;
import com.star_1mp.powersunleashed.web.WebCartridges;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Web cartridge: {@link WebCartridges#MAX_FLUID} shots of web fluid for the web-shooters. Using it while wearing
 * web-shooters loads it straight into them (swapping out a used-up cartridge if both slots are taken).
 */
public class WebCartridgeItem extends Item {

    public WebCartridgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack shooters = WebCartridges.wornShooters(player);
        if (shooters.isEmpty() || !WebCartridges.isLoadable(held)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            // the server decides; keep the client from using the item in the meantime
            return WebCartridges.contents(shooters).size() < com.star_1mp.powersunleashed.tech.WebModules.cartridgeSlots(shooters) || WebCartridges.contents(shooters).contains(0)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        ItemStack[] result = {null};
        Services.GAMEPLAY.modifyAccessory(player, PUItems.WEB_SHOOTERS.get(), worn -> result[0] = WebCartridges.insertOrSwap(worn, held));
        ItemStack swapped = result[0];
        if (swapped == null) {
            return InteractionResult.PASS;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (!swapped.isEmpty() && !player.getInventory().add(swapped) && level instanceof ServerLevel serverLevel) {
            player.spawnAtLocation(serverLevel, swapped);  // inventory full: drop it
        }
        WebCartridges.playInsertSound(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return WebCartridges.fluid(stack) < WebCartridges.MAX_FLUID;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0F * WebCartridges.fluid(stack) / WebCartridges.MAX_FLUID), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE6EEF5;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.powers_unleashed.web_fluid", WebCartridges.fluid(stack), WebCartridges.MAX_FLUID)
                .withStyle(ChatFormatting.GRAY));
    }
}
