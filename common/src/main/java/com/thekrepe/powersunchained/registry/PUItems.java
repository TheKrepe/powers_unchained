package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.item.SuitPieceItem;
import com.thekrepe.powersunchained.item.WebShootersItem;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.suit.Suit;
import com.thekrepe.powersunchained.suit.Suits;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public final class PUItems {

    private static final List<Supplier<? extends Item>> SUITS = new ArrayList<>();
    private static final List<Supplier<? extends Item>> WEAPONS = new ArrayList<>();
    private static final List<Supplier<? extends Item>> ITEMS = new ArrayList<>();
    private static final List<Supplier<? extends Item>> BLOCKS = new ArrayList<>();

    /** Pieces in tab order: mask, chestplate, pants, boots. */
    private static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    // Items: fabric in every dye color, made from wool and a stick
    public static final Map<DyeColor, Supplier<Item>> FABRICS = registerFabrics();

    // Weapons & gadgets
    public static final Supplier<WebShootersItem> WEB_SHOOTERS = register(WEAPONS, "web_shooters", WebShootersItem::new,
            properties -> properties.stacksTo(1));

    // Blocks
    public static final Supplier<BlockItem> SUIT_CREATOR = blockItem("suit_creator", PUBlocks.SUIT_CREATOR);

    // Suits: four pieces for every suit in Suits.ALL
    private static final Map<Suit, Map<ArmorType, Supplier<SuitPieceItem>>> SUIT_PIECES = registerSuits();

    public static final Supplier<SuitPieceItem> SPIDER_MAN_MASK = piece(Suits.SPIDER_MAN, ArmorType.HELMET);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_CHESTPLATE = piece(Suits.SPIDER_MAN, ArmorType.CHESTPLATE);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_PANTS = piece(Suits.SPIDER_MAN, ArmorType.LEGGINGS);
    public static final Supplier<SuitPieceItem> SPIDER_MAN_BOOTS = piece(Suits.SPIDER_MAN, ArmorType.BOOTS);

    private PUItems() {
    }

    /** Called from {@code PowersUnchained.init()} to make sure everything above gets registered. */
    public static void init() {
    }

    public static List<Supplier<? extends Item>> suits() {
        return Collections.unmodifiableList(SUITS);
    }

    public static List<Supplier<? extends Item>> weapons() {
        return Collections.unmodifiableList(WEAPONS);
    }

    public static List<Supplier<? extends Item>> items() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static List<Supplier<? extends Item>> blocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static Supplier<SuitPieceItem> piece(Suit suit, ArmorType type) {
        return SUIT_PIECES.get(suit).get(type);
    }

    /** The same piece of the suit variant {@code offset} steps away (see {@link Suits#VARIANT_GROUPS}). */
    public static SuitPieceItem variant(SuitPieceItem item, int offset) {
        return piece(Suits.variant(item.getSuit(), offset), item.getArmorType()).get();
    }

    private static Map<DyeColor, Supplier<Item>> registerFabrics() {
        Map<DyeColor, Supplier<Item>> fabrics = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            Supplier<Item> item = Services.REGISTRY.registerItem(color.getSerializedName() + "_fabric", Item::new, UnaryOperator.identity());
            fabrics.put(color, item);
            ITEMS.add(item);
        }
        return Collections.unmodifiableMap(fabrics);
    }

    private static <T extends Item> Supplier<T> register(List<Supplier<? extends Item>> tab, String name,
            java.util.function.Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties) {
        Supplier<T> item = Services.REGISTRY.registerItem(name, factory, properties);
        tab.add(item);
        return item;
    }

    private static Supplier<BlockItem> blockItem(String name, Supplier<? extends Block> block) {
        Supplier<BlockItem> item = Services.REGISTRY.registerItem(
                name,
                properties -> new BlockItem(block.get(), properties),
                Item.Properties::useBlockDescriptionPrefix);
        BLOCKS.add(item);
        return item;
    }

    private static Map<Suit, Map<ArmorType, Supplier<SuitPieceItem>>> registerSuits() {
        Map<Suit, Map<ArmorType, Supplier<SuitPieceItem>>> all = new LinkedHashMap<>();
        for (Suit suit : Suits.ALL) {
            Map<ArmorType, Supplier<SuitPieceItem>> pieces = new EnumMap<>(ArmorType.class);
            for (ArmorType type : PIECES) {
                String name = suit.id().getPath() + "_" + pieceName(type);
                Supplier<SuitPieceItem> item = Services.REGISTRY.registerItem(
                        name,
                        properties -> new SuitPieceItem(properties, suit, type),
                        properties -> SuitPieceItem.properties(properties, suit, type));
                pieces.put(type, item);
                SUITS.add(item);
            }
            all.put(suit, pieces);
        }
        return all;
    }

    private static String pieceName(ArmorType type) {
        return switch (type) {
            case HELMET -> "mask";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "pants";
            default -> "boots";
        };
    }
}
