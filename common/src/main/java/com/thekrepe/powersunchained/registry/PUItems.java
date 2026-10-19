package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.suit.Suit;
import com.thekrepe.powersunchained.suit.Suits;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.equipment.ArmorType;

public final class PUItems {

    private static final List<Supplier<? extends Item>> ALL = new ArrayList<>();

    // Materials: fabric in every dye color, made from wool and a stick
    public static final Map<DyeColor, Supplier<Item>> FABRICS = registerFabrics();

    // Blocks
    public static final Supplier<BlockItem> SUIT_CREATOR = blockItem("suit_creator", PUBlocks.SUIT_CREATOR);

    // Spider-Man
    public static final Supplier<SuitPieceItem> SPIDER_MAN_MASK = suitPiece("spider_man_mask", Suits.SPIDER_MAN, ArmorType.HELMET);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_SUIT = suitPiece("spider_man_suit", Suits.SPIDER_MAN, ArmorType.CHESTPLATE);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_PANTS = suitPiece("spider_man_pants", Suits.SPIDER_MAN, ArmorType.LEGGINGS);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_BOOTS = suitPiece("spider_man_boots", Suits.SPIDER_MAN, ArmorType.BOOTS);

    private PUItems() {
    }

    /** Called from {@code PowersUnchained.init()} to make sure everything above gets registered. */
    public static void init() {
    }

    /** All items of the mod, in creative-tab order. */
    public static List<Supplier<? extends Item>> all() {
        return Collections.unmodifiableList(ALL);
    }

    private static Map<DyeColor, Supplier<Item>> registerFabrics() {
        Map<DyeColor, Supplier<Item>> fabrics = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            Supplier<Item> item = Services.REGISTRY.registerItem(color.getSerializedName() + "_fabric", Item::new, UnaryOperator.identity());
            fabrics.put(color, item);
            ALL.add(item);
        }
        return Collections.unmodifiableMap(fabrics);
    }

    private static Supplier<BlockItem> blockItem(String name, Supplier<? extends Block> block) {
        Supplier<BlockItem> item = Services.REGISTRY.registerItem(
                name,
                properties -> new BlockItem(block.get(), properties),
                Item.Properties::useBlockDescriptionPrefix);
        ALL.add(item);
        return item;
    }

    private static Supplier<SuitPieceItem> suitPiece(String name, Suit suit, ArmorType type) {
        Supplier<SuitPieceItem> item = Services.REGISTRY.registerItem(
                name,
                properties -> new SuitPieceItem(properties, suit, type),
                properties -> SuitPieceItem.properties(properties, suit, type));
        ALL.add(item);
        return item;
    }
}
