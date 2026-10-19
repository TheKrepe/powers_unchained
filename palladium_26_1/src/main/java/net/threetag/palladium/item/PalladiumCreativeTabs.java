package net.threetag.palladium.item;

import net.minecraft.core.Holder;
import net.minecraft.util.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Comparator;

import static net.threetag.palladium.item.PalladiumItems.*;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class PalladiumCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Palladium.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable(Util.makeDescriptionId("itemGroup", Palladium.id("main"))))
            .icon(PalladiumItems.SUIT_STAND::toStack)
            .displayItems(ITEMS.getEntries())
            .build()
    );

    @SubscribeEvent
    static void creativeTabPlacement(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey().equals(CreativeModeTabs.REDSTONE_BLOCKS) || e.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            e.insertAfter(Items.ARMOR_STAND.getDefaultInstance(), SUIT_STAND.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (e.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            e.insertAfter(Items.LOOM.getDefaultInstance(), TAILORING_BENCH.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            var anchor = TAILORING_BENCH.get().isEnabled(e.getFlags()) ? TAILORING_BENCH.get().getDefaultInstance() : Items.LOOM.getDefaultInstance();
            e.insertAfter(anchor, MULTIVERSAL_ITERATOR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (e.getTabKey().equals(MAIN.getKey()) && MULTIVERSAL_EXTRAPOLATOR.get().isEnabled(e.getFlags())) {
            e.getParameters().holders().lookup(PalladiumRegistryKeys.MULTIVERSE).ifPresent(lookup -> {
                var previous = MULTIVERSAL_EXTRAPOLATOR.get().getDefaultInstance();
                var universes = lookup.listElements()
                        .sorted(Comparator.comparing(ref -> ref.key().identifier().toString()))
                        .toList();

                for (Holder.Reference<Universe> universe : universes) {
                    var stack = MULTIVERSAL_EXTRAPOLATOR.get().getDefaultInstance();
                    MultiversalExtrapolatorItem.setUniverse(stack, universe.key());
                    e.insertAfter(previous, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    previous = stack;
                }
            });
        }

        if (e.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            e.insertAfter(Items.RAW_COPPER.getDefaultInstance(), RAW_LEAD.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.RAW_GOLD.getDefaultInstance(), RAW_TITANIUM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(RAW_TITANIUM.get().getDefaultInstance(), RAW_VIBRANIUM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.COPPER_NUGGET.getDefaultInstance(), LEAD_NUGGET.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.GOLD_NUGGET.getDefaultInstance(), TITANIUM_NUGGET.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(TITANIUM_NUGGET.get().getDefaultInstance(), VIBRANIUM_NUGGET.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.COPPER_INGOT.getDefaultInstance(), LEAD_INGOT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.NETHERITE_INGOT.getDefaultInstance(), TITANIUM_INGOT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(TITANIUM_INGOT.get().getDefaultInstance(), VIBRANIUM_INGOT.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);

            e.insertAfter(Items.PINK_DYE.getDefaultInstance(), WHITE_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(WHITE_FABRIC.toStack(), LIGHT_GRAY_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(LIGHT_GRAY_FABRIC.toStack(), GRAY_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(GRAY_FABRIC.toStack(), BLACK_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(BLACK_FABRIC.toStack(), BROWN_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(BROWN_FABRIC.toStack(), RED_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(RED_FABRIC.toStack(), ORANGE_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(ORANGE_FABRIC.toStack(), YELLOW_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(YELLOW_FABRIC.toStack(), LIME_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(LIME_FABRIC.toStack(), GREEN_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(GREEN_FABRIC.toStack(), CYAN_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(CYAN_FABRIC.toStack(), LIGHT_BLUE_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(LIGHT_BLUE_FABRIC.toStack(), BLUE_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(BLUE_FABRIC.toStack(), PURPLE_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(PURPLE_FABRIC.toStack(), MAGENTA_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(MAGENTA_FABRIC.toStack(), PINK_FABRIC.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (e.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS)) {
            e.insertAfter(Items.LAPIS_BLOCK.getDefaultInstance(), LEAD_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.NETHERITE_BLOCK.getDefaultInstance(), TITANIUM_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(TITANIUM_BLOCK.get().getDefaultInstance(), VIBRANIUM_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.CHISELED_TUFF_BRICKS.getDefaultInstance(), METEORITE_STONE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_STONE.get().getDefaultInstance(), METEORITE_STONE_STAIRS.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_STONE_STAIRS.get().getDefaultInstance(), METEORITE_STONE_SLAB.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_STONE_SLAB.get().getDefaultInstance(), METEORITE_STONE_WALL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_STONE_WALL.get().getDefaultInstance(), METEORITE_BRICKS.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_BRICKS.get().getDefaultInstance(), METEORITE_BRICK_STAIRS.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_BRICK_STAIRS.get().getDefaultInstance(), METEORITE_BRICK_SLAB.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_BRICK_SLAB.get().getDefaultInstance(), METEORITE_BRICK_WALL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (e.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)) {
            e.insertAfter(Items.WITHER_ROSE.getDefaultInstance(), HEART_SHAPED_HERB.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.POINTED_DRIPSTONE.getDefaultInstance(), METEORITE_STONE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_COAL_ORE.getDefaultInstance(), METEORITE_COAL_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_IRON_ORE.getDefaultInstance(), METEORITE_IRON_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_COPPER_ORE.getDefaultInstance(), METEORITE_COPPER_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_COPPER_ORE.get().getDefaultInstance(), LEAD_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(LEAD_ORE.get().getDefaultInstance(), DEEPSLATE_LEAD_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(DEEPSLATE_LEAD_ORE.get().getDefaultInstance(), METEORITE_LEAD_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_GOLD_ORE.getDefaultInstance(), METEORITE_GOLD_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_REDSTONE_ORE.getDefaultInstance(), METEORITE_REDSTONE_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_EMERALD_ORE.getDefaultInstance(), METEORITE_EMERALD_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_LAPIS_ORE.getDefaultInstance(), METEORITE_LAPIS_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.DEEPSLATE_DIAMOND_ORE.getDefaultInstance(), METEORITE_DIAMOND_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_DIAMOND_ORE.get().getDefaultInstance(), TITANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(TITANIUM_ORE.get().getDefaultInstance(), DEEPSLATE_TITANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(DEEPSLATE_TITANIUM_ORE.get().getDefaultInstance(), METEORITE_TITANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_TITANIUM_ORE.get().getDefaultInstance(), VIBRANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(VIBRANIUM_ORE.get().getDefaultInstance(), DEEPSLATE_VIBRANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(DEEPSLATE_VIBRANIUM_ORE.get().getDefaultInstance(), METEORITE_VIBRANIUM_ORE.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(METEORITE_VIBRANIUM_ORE.get().getDefaultInstance(), METEORITE_VIBRANIUM_VEIN.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.RAW_COPPER_BLOCK.getDefaultInstance(), RAW_LEAD_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(Items.RAW_GOLD_BLOCK.getDefaultInstance(), RAW_TITANIUM_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            e.insertAfter(RAW_TITANIUM_BLOCK.get().getDefaultInstance(), RAW_VIBRANIUM_BLOCK.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

}
