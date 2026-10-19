package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.platform.services.IRegistryHelper;
import com.thekrepe.powersunchained.suit.Suits;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

    /** Empty for now. Vanilla hides tabs without items, so it shows up once the first weapon exists. */
    public static final Supplier<CreativeModeTab> WEAPONS = tab("weapons", "suits",
            Items.IRON_SWORD::getDefaultInstance, output -> all(output, PUItems.weapons()));

    public static final Supplier<CreativeModeTab> ITEMS = tab("items", "weapons",
            () -> PUItems.FABRICS.get(DyeColor.RED).get().getDefaultInstance(),
            output -> all(output, PUItems.items()));

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
