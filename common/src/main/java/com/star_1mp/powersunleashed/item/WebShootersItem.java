package com.star_1mp.powersunleashed.item;

import com.star_1mp.powersunleashed.web.WebCartridges;
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

    /**
     * Creative web-shooters: the same item with {@code powers_unleashed:creative_web_shooters} — endless web fluid,
     * no cartridges, enchantment glint on the item (not on the player's wrists). Operator Utilities tab.
     */
    public static ItemStack creative() {
        ItemStack stack = new ItemStack(com.star_1mp.powersunleashed.registry.PUItems.WEB_SHOOTERS.get());
        stack.set(com.star_1mp.powersunleashed.registry.PUDataComponents.CREATIVE_WEB_SHOOTERS.get(), true);
        stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    public static boolean isCreative(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(com.star_1mp.powersunleashed.registry.PUDataComponents.CREATIVE_WEB_SHOOTERS.get()));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player) {
        if (self.getCount() != 1 || isCreative(self)) {
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
        if (self.getCount() != 1 || isCreative(self) || !slot.allowModification(player)) {
            return false;
        }
        if (!other.isEmpty()) {
            if (!WebCartridges.isLoadable(other)) {
                return false;
            }
            if (WebCartridges.insert(self, other)) {
                other.shrink(1);
                slot.set(self);  // some slots (Curios) hand out a copy: write the change back
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
                slot.set(self);  // some slots (Curios) hand out a copy: without this the cartridge stays inside too
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
        return !isCreative(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0F * WebCartridges.totalFluid(stack) / (WebCartridges.MAX_FLUID * com.star_1mp.powersunleashed.tech.WebModules.cartridgeSlots(stack))), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        if (isCreative(stack)) {
            builder.accept(Component.translatable("tooltip.powers_unleashed.web_shooters.creative").withStyle(ChatFormatting.LIGHT_PURPLE));
            builder.accept(Component.translatable("tooltip.powers_unleashed.web_shooters.endless").withStyle(ChatFormatting.GRAY));
            return;
        }
        List<Integer> contents = WebCartridges.contents(stack);
        builder.accept(Component.translatable("tooltip.powers_unleashed.web_shooters.cartridges", contents.size(), com.star_1mp.powersunleashed.tech.WebModules.cartridgeSlots(stack))
                .withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("tooltip.powers_unleashed.web_fluid", WebCartridges.totalFluid(stack),
                WebCartridges.MAX_FLUID * contents.size()).withStyle(ChatFormatting.GRAY));
        if (contents.isEmpty()) {
            builder.accept(Component.translatable("tooltip.powers_unleashed.web_shooters.load").withStyle(ChatFormatting.DARK_GRAY));
        }
        for (com.star_1mp.powersunleashed.tech.WebModule module : com.star_1mp.powersunleashed.tech.WebModules.installed(stack)) {
            builder.accept(Component.literal(" + ").append(module.displayName()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return isCreative(stack) ? Optional.empty() : Optional.of(new WebShootersTooltip(WebCartridges.contents(stack), com.star_1mp.powersunleashed.tech.WebModules.cartridgeSlots(stack)));
    }

    /** Tooltip image: the cartridge slots with the loaded cartridges. */
    public record WebShootersTooltip(List<Integer> cartridges, int slots) implements TooltipComponent {
    }
}
