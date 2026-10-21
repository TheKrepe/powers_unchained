package com.thekrepe.powersunchained.item;

import com.thekrepe.powersunchained.web.WebCartridges;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Web-Shooters: wrist devices worn in the Curios "bracelet" slot (Trinkets on Fabric later). Hold up to two web
 * cartridges, loaded like items into a bundle: click a cartridge onto the web-shooters (or the web-shooters onto a
 * cartridge), right click with an empty cursor takes the last one out. The bar shows the web fluid left.
 */
public class WebShootersItem extends Item {

    private static final int BAR_COLOR = 0xE6EEF5;

    public WebShootersItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player) {
        if (self.getCount() != 1) {
            return false;
        }
        ItemStack other = slot.getItem();
        if (!other.isEmpty()) {
            if (!WebCartridges.isLoadable(other)) {
                return false;
            }
            if (WebCartridges.insert(self, other)) {
                slot.safeTake(1, 1, player);
                WebCartridges.playInsertSound(player);
            } else {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            }
            changed(player);
            return true;
        }
        if (action == ClickAction.SECONDARY) {
            ItemStack removed = WebCartridges.removeLast(self);
            if (!removed.isEmpty()) {
                ItemStack rest = slot.safeInsert(removed);
                if (!rest.isEmpty()) {
                    WebCartridges.insert(self, rest);  // didn't fit: put it back
                } else {
                    WebCartridges.playRemoveSound(player);
                }
                changed(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player,
            SlotAccess carried) {
        if (self.getCount() != 1 || !slot.allowModification(player)) {
            return false;
        }
        if (!other.isEmpty()) {
            if (!WebCartridges.isLoadable(other)) {
                return false;
            }
            if (WebCartridges.insert(self, other)) {
                other.shrink(1);
                WebCartridges.playInsertSound(player);
            } else {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            }
            changed(player);
            return true;
        }
        if (action == ClickAction.SECONDARY) {
            ItemStack removed = WebCartridges.removeLast(self);
            if (!removed.isEmpty()) {
                carried.set(removed);
                WebCartridges.playRemoveSound(player);
                changed(player);
            }
            return true;
        }
        return false;
    }

    private static void changed(Player player) {
        if (player.containerMenu != null) {
            player.containerMenu.slotsChanged(player.getInventory());
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0F * WebCartridges.totalFluid(stack) / (WebCartridges.MAX_FLUID * WebCartridges.SLOTS)), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        List<Integer> contents = WebCartridges.contents(stack);
        builder.accept(Component.translatable("tooltip.powers_unchained.web_shooters.cartridges", contents.size(), WebCartridges.SLOTS)
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.powers_unchained.web_fluid", WebCartridges.totalFluid(stack),
                WebCartridges.MAX_FLUID * contents.size()).withStyle(ChatFormatting.GRAY));
        if (contents.isEmpty()) {
            builder.accept(Component.translatable("tooltip.powers_unchained.web_shooters.load").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new WebShootersTooltip(WebCartridges.contents(stack)));
    }

    /** Tooltip image: the cartridge slots with the loaded cartridges. */
    public record WebShootersTooltip(List<Integer> cartridges) implements TooltipComponent {
    }
}
