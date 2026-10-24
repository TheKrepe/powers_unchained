package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.item.SuitPieceItem;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.platform.services.IRegistryHelper;
import com.star_1mp.powersunchained.suit.Suits;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Creative tabs, in this order: Suits, Weapons, Items, Blocks. */
public final class PUCreativeTabs {

    /**
     * Suit pieces. Only the first variant of each hero is listed; the other variants are reachable with the
     * mouse wheel over a piece (client: {@code SuitVariantScroller}) and through search.
     */
    public static final Supplier<CreativeModeTab> SUITS = tab("suits", null,
            () -> PUItems.SPIDER_MAN_MASK.get().getDefaultInstance(),
            output -> PUItems.suits().forEach(item -> {
                boolean primary = !(item.get() instanceof SuitPieceItem piece) || Suits.isPrimaryVariant(piece.getSuit());
                output.accept(item.get().getDefaultInstance(), !primary);
            }));

    /** Weapons and gadgets (web-shooters, ...). */
    public static final Supplier<CreativeModeTab> WEAPONS = tab("weapons", "suits",
            () -> PUItems.WEB_SHOOTERS.get().getDefaultInstance(), output -> all(output, PUItems.weapons()));

    public static final Supplier<CreativeModeTab> ITEMS = tab("items", "weapons",
            () -> PUItems.FABRICS.get(DyeColor.RED).get().getDefaultInstance(),
            output -> {
                PUItems.items().stream().filter(item -> item != PUItems.SUIT_FRAGMENT)
                        .forEach(item -> output.accept(item.get().getDefaultInstance(), false));
                // one fragment per suit
                com.star_1mp.powersunchained.suit.Suits.ALL.forEach(suit ->
                        output.accept(com.star_1mp.powersunchained.item.SuitFragmentItem.of(suit), false));
            });

    public static final Supplier<CreativeModeTab> BLOCKS = tab("blocks", "items",
            () -> PUItems.SUIT_CREATOR.get().getDefaultInstance(), output -> all(output, PUItems.blocks()));

    private PUCreativeTabs() {
    }

    public static void init() {
    }

    private static void all(IRegistryHelper.TabOutput output, List<Supplier<? extends Item>> items) {
        items.forEach(item -> output.accept(item.get().getDefaultInstance(), false));
    }

    private static Supplier<CreativeModeTab> tab(String name, @Nullable String after, Supplier<ItemStack> icon,
            Consumer<IRegistryHelper.TabOutput> contents) {
        Identifier afterId = after == null ? null : Constants.id(after);
        return Services.REGISTRY.registerCreativeTab(name, Component.translatable("itemGroup.powers_unchained." + name),
                icon, contents, afterId);
    }
}
