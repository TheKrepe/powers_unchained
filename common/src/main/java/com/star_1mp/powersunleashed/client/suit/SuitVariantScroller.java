package com.star_1mp.powersunleashed.client.suit;

import com.star_1mp.powersunleashed.item.SuitPieceItem;
import com.star_1mp.powersunleashed.registry.PUCreativeTabs;
import com.star_1mp.powersunleashed.registry.PUItems;
import com.star_1mp.powersunleashed.suit.Suit;
import com.star_1mp.powersunleashed.suit.Suits;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Suit variants in the Suits creative tab (like alt versions in Fisk's Superheroes, but every variant is its own
 * item): the tab lists only the first variant of each hero, the mouse wheel over any piece swaps the whole suit
 * (all four pieces) to the previous / next variant.
 */
public final class SuitVariantScroller {

    private SuitVariantScroller() {
    }

    /** @return {@code true} if the scroll was used (then the creative list must not scroll) */
    public static boolean onScroll(Screen screen, @Nullable Slot hovered, double scrollY) {
        if (scrollY == 0 || !(screen instanceof CreativeModeInventoryScreen creative)) {
            return false;
        }
        if (hovered == null || hovered.container instanceof Inventory
                || !(hovered.getItem().getItem() instanceof SuitPieceItem piece)
                || Suits.variantPosition(piece.getSuit()) == null
                || !isSuitsTabOpen(creative)) {
            return false;
        }
        Suit current = piece.getSuit();
        // wheel up = next variant, wheel down = previous
        Suit next = Suits.variant(current, scrollY > 0 ? 1 : -1);
        // the tab contents (kept when the list is scrolled) ...
        List<ItemStack> items = creative.getMenu().items;
        for (int i = 0; i < items.size(); i++) {
            ItemStack swapped = swap(items.get(i), current, next);
            if (swapped != null) {
                items.set(i, swapped);
            }
        }
        // ... and what is visible right now
        for (Slot slot : creative.getMenu().slots) {
            if (!(slot.container instanceof Inventory)) {
                ItemStack swapped = swap(slot.getItem(), current, next);
                if (swapped != null) {
                    slot.set(swapped);
                }
            }
        }
        return true;
    }

    private static @Nullable ItemStack swap(ItemStack stack, Suit from, Suit to) {
        if (stack.getItem() instanceof SuitPieceItem p && p.getSuit() == from) {
            return PUItems.piece(to, p.getArmorType()).get().getDefaultInstance();
        }
        return null;
    }

    /** Soft blue for the variant line. */
    private static final int VARIANT_COLOR = 0x8FB8FF;

    /**
     * Adds the variant line right under the item name ("Variant 2/5: Advanced 2.0") and the mouse-wheel hint —
     * only for suit pieces shown in the Suits creative tab, not for items anywhere else.
     */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!(stack.getItem() instanceof SuitPieceItem piece) || tooltip.isEmpty()) {
            return;
        }
        int[] position = Suits.variantPosition(piece.getSuit());
        if (position == null || !(Minecraft.getInstance().gui.screen() instanceof CreativeModeInventoryScreen creative)
                || !isSuitsTabOpen(creative)) {
            return;
        }
        Component name = Component.translatable(Suits.variantNameKey(piece.getSuit()));
        tooltip.add(1, Component.translatable("tooltip.powers_unleashed.suit_variant", position[0] + 1, position[1], name)
                .withColor(VARIANT_COLOR));
        tooltip.add(2, Component.translatable("tooltip.powers_unleashed.suit_variant.scroll")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * The open creative page is the Suits tab: same length as the tab contents and, slot by slot, the same item
     * or the same piece of another variant of the same hero. (The selected tab itself is private in vanilla.)
     */
    private static boolean isSuitsTabOpen(CreativeModeInventoryScreen screen) {
        List<ItemStack> items = screen.getMenu().items;
        var display = PUCreativeTabs.SUITS.get().getDisplayItems();
        if (items.size() != display.size() || items.isEmpty()) {
            return false;
        }
        Iterator<ItemStack> expected = display.iterator();
        for (ItemStack stack : items) {
            ItemStack other = expected.next();
            if (stack.getItem() == other.getItem()) {
                continue;
            }
            if (!(stack.getItem() instanceof SuitPieceItem a) || !(other.getItem() instanceof SuitPieceItem b)
                    || a.getArmorType() != b.getArmorType() || !sameGroup(a, b)) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameGroup(SuitPieceItem a, SuitPieceItem b) {
        return Suits.VARIANT_GROUPS.stream().anyMatch(g -> g.contains(a.getSuit()) && g.contains(b.getSuit()));
    }
}
