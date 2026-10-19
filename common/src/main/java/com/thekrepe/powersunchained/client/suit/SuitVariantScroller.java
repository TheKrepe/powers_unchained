package com.thekrepe.powersunchained.client.suit;

import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.registry.PUCreativeTabs;
import com.thekrepe.powersunchained.registry.PUItems;
import com.thekrepe.powersunchained.suit.Suits;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Suit variants in the Suits creative tab (like alt versions in Fisk's Superheroes, but every variant is its own
 * item): the tab lists only the first variant of each hero, the mouse wheel over a piece swaps it for the same
 * piece of the previous / next variant.
 */
public final class SuitVariantScroller {

    /** The screen that is open right now (set by the loader from screen open / close events). */
    private static @Nullable Screen openScreen;

    private SuitVariantScroller() {
    }

    public static void setOpenScreen(@Nullable Screen screen) {
        openScreen = screen;
    }

    /** @return {@code true} if the scroll was used (then the creative list must not scroll) */
    public static boolean onScroll(Screen screen, @Nullable Slot slot, double scrollY) {
        if (scrollY == 0 || !(screen instanceof CreativeModeInventoryScreen creative)) {
            return false;
        }
        if (slot == null || slot.container instanceof Inventory
                || !(slot.getItem().getItem() instanceof SuitPieceItem piece)
                || Suits.variantPosition(piece.getSuit()) == null
                || !isSuitsTabOpen(creative)) {
            return false;
        }
        SuitPieceItem next = PUItems.variant(piece, scrollY > 0 ? -1 : 1);
        List<ItemStack> items = creative.getMenu().items;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).is(piece)) {
                items.set(i, next.getDefaultInstance());
                break;
            }
        }
        slot.set(next.getDefaultInstance());
        return true;
    }

    /** Adds "variant 2/5 — scroll to switch" to suit pieces in the Suits tab. */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!(stack.getItem() instanceof SuitPieceItem piece)) {
            return;
        }
        int[] position = Suits.variantPosition(piece.getSuit());
        if (position == null || !(openScreen instanceof CreativeModeInventoryScreen creative)
                || !isSuitsTabOpen(creative)) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.powers_unchained.suit_variant", position[0] + 1, position[1])
                .withStyle(ChatFormatting.GRAY));
    }

    /**
     * The open creative page is the Suits tab: same length as the tab contents and, slot by slot, the same item
     * or another variant of the same piece. (The selected tab itself is private in vanilla.)
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
