package net.threetag.palladium.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.flag.PalladiumFeatureFlags;

import java.util.EnumMap;

public class PalladiumItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Palladium.MOD_ID);
    public static final EnumMap<DyeColor, DeferredItem<Item>> FABRIC_BY_COLOR = new EnumMap<>(DyeColor.class);

    // Utilities
    public static final DeferredItem<BlockItem> TAILORING_BENCH = ITEMS.registerSimpleBlockItem(PalladiumBlocks.TAILORING_BENCH, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MECHANIC_TAILORING));
    public static final DeferredItem<BlockItem> MULTIVERSAL_ITERATOR = ITEMS.registerSimpleBlockItem(PalladiumBlocks.MULTIVERSAL_ITERATOR, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MECHANIC_MULTIVERSAL_VARIANTS));
    public static final DeferredItem<Item> MULTIVERSAL_EXTRAPOLATOR = ITEMS.registerItem("multiversal_extrapolator", MultiversalExtrapolatorItem::new, properties -> properties.durability(4).requiredFeatures(PalladiumFeatureFlags.MECHANIC_MULTIVERSAL_VARIANTS));
    public static final DeferredItem<VialItem> VIAL = ITEMS.registerItem("vial", properties -> new VialItem(Fluids.EMPTY, properties), properties -> properties.stacksTo(16).requiredFeatures(PalladiumFeatureFlags.ITEM_VIALS));
    public static final DeferredItem<Item> SUIT_STAND = ITEMS.registerItem("suit_stand", SuitStandItem::new);
    public static final DeferredItem<BlockItem> HEART_SHAPED_HERB = ITEMS.registerSimpleBlockItem(PalladiumBlocks.HEART_SHAPED_HERB, properties -> properties.requiredFeatures(PalladiumFeatureFlags.BLOCK_HEART_SHAPED_HERB));

    public static final DeferredItem<Item> LEAD_CIRCUIT = ITEMS.registerSimpleItem("lead_circuit", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_CIRCUITS, PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<Item> QUARTZ_CIRCUIT = ITEMS.registerSimpleItem("quartz_circuit", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_CIRCUITS));
    public static final DeferredItem<Item> VIBRANIUM_CIRCUIT = ITEMS.registerSimpleItem("vibranium_circuit", properties -> properties.rarity(Rarity.RARE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_CIRCUITS, PalladiumFeatureFlags.MATERIAL_VIBRANIUM));

    // Resource Blocks
    public static final DeferredItem<BlockItem> METEORITE_STONE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_STONE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_STONE_STAIRS = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_STONE_STAIRS, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_STONE_WALL = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_STONE_WALL, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_STONE_SLAB = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_STONE_SLAB, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_BRICKS = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_BRICKS, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_BRICK_STAIRS = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_BRICK_STAIRS, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_BRICK_WALL = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_BRICK_WALL, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_BRICK_SLAB = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_BRICK_SLAB, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));

    // Materials
    public static final DeferredItem<BlockItem> LEAD_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.LEAD_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<BlockItem> DEEPSLATE_LEAD_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.DEEPSLATE_LEAD_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<BlockItem> METEORITE_LEAD_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_LEAD_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> LEAD_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.LEAD_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<BlockItem> RAW_LEAD_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.RAW_LEAD_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<Item> RAW_LEAD = ITEMS.registerSimpleItem("raw_lead", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<Item> LEAD_INGOT = ITEMS.registerSimpleItem("lead_ingot", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredItem<Item> LEAD_NUGGET = ITEMS.registerSimpleItem("lead_nugget", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));

    public static final DeferredItem<BlockItem> TITANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.TITANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<BlockItem> DEEPSLATE_TITANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.DEEPSLATE_TITANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<BlockItem> METEORITE_TITANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_TITANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> TITANIUM_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.TITANIUM_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<BlockItem> RAW_TITANIUM_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.RAW_TITANIUM_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredItem<Item> TITANIUM_NUGGET = ITEMS.registerSimpleItem("titanium_nugget", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));

    public static final DeferredItem<BlockItem> VIBRANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.VIBRANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<BlockItem> DEEPSLATE_VIBRANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.DEEPSLATE_VIBRANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<BlockItem> METEORITE_VIBRANIUM_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_VIBRANIUM_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_VIBRANIUM_VEIN = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_VIBRANIUM_VEIN, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> VIBRANIUM_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.VIBRANIUM_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<BlockItem> RAW_VIBRANIUM_BLOCK = ITEMS.registerSimpleBlockItem(PalladiumBlocks.RAW_VIBRANIUM_BLOCK, properties -> properties.requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<Item> RAW_VIBRANIUM = ITEMS.registerSimpleItem("raw_vibranium", properties -> properties.rarity(Rarity.EPIC).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<Item> VIBRANIUM_INGOT = ITEMS.registerSimpleItem("vibranium_ingot", properties -> properties.rarity(Rarity.EPIC).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredItem<Item> VIBRANIUM_NUGGET = ITEMS.registerSimpleItem("vibranium_nugget", properties -> properties.rarity(Rarity.EPIC).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));

    public static final DeferredItem<BlockItem> METEORITE_COAL_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_COAL_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_IRON_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_IRON_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_COPPER_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_COPPER_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_GOLD_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_GOLD_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_REDSTONE_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_REDSTONE_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_EMERALD_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_EMERALD_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_LAPIS_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_LAPIS_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredItem<BlockItem> METEORITE_DIAMOND_ORE = ITEMS.registerSimpleBlockItem(PalladiumBlocks.METEORITE_DIAMOND_ORE, properties -> properties.requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));

    // Ingredients
    public static final DeferredItem<Item> WHITE_FABRIC = registerFabric(DyeColor.WHITE);
    public static final DeferredItem<Item> LIGHT_GRAY_FABRIC = registerFabric(DyeColor.LIGHT_GRAY);
    public static final DeferredItem<Item> GRAY_FABRIC = registerFabric(DyeColor.GRAY);
    public static final DeferredItem<Item> BLACK_FABRIC = registerFabric(DyeColor.BLACK);
    public static final DeferredItem<Item> BROWN_FABRIC = registerFabric(DyeColor.BROWN);
    public static final DeferredItem<Item> RED_FABRIC = registerFabric(DyeColor.RED);
    public static final DeferredItem<Item> ORANGE_FABRIC = registerFabric(DyeColor.ORANGE);
    public static final DeferredItem<Item> YELLOW_FABRIC = registerFabric(DyeColor.YELLOW);
    public static final DeferredItem<Item> LIME_FABRIC = registerFabric(DyeColor.LIME);
    public static final DeferredItem<Item> GREEN_FABRIC = registerFabric(DyeColor.GREEN);
    public static final DeferredItem<Item> CYAN_FABRIC = registerFabric(DyeColor.CYAN);
    public static final DeferredItem<Item> LIGHT_BLUE_FABRIC = registerFabric(DyeColor.LIGHT_BLUE);
    public static final DeferredItem<Item> BLUE_FABRIC = registerFabric(DyeColor.BLUE);
    public static final DeferredItem<Item> PURPLE_FABRIC = registerFabric(DyeColor.PURPLE);
    public static final DeferredItem<Item> MAGENTA_FABRIC = registerFabric(DyeColor.MAGENTA);
    public static final DeferredItem<Item> PINK_FABRIC = registerFabric(DyeColor.PINK);

    private static DeferredItem<Item> registerFabric(DyeColor color) {
        var supplier = ITEMS.registerSimpleItem(color.getName() + "_fabric", properties -> properties.requiredFeatures(PalladiumFeatureFlags.MECHANIC_TAILORING));
        FABRIC_BY_COLOR.put(color, supplier);
        return supplier;
    }

}
