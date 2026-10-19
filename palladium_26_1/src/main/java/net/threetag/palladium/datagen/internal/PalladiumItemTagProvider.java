package net.threetag.palladium.datagen.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.tag.PalladiumItemTags;

import java.util.concurrent.CompletableFuture;

public class PalladiumItemTagProvider extends ItemTagsProvider {

    public PalladiumItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Palladium.MOD_ID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ItemTags.SMALL_FLOWERS).add(PalladiumItems.HEART_SHAPED_HERB.get());
        this.tag(PalladiumItemTags.LEAD_ORES).add(PalladiumItems.LEAD_ORE.get(), PalladiumItems.DEEPSLATE_LEAD_ORE.get(), PalladiumItems.METEORITE_LEAD_ORE.get());
        this.tag(PalladiumItemTags.LEAD_STORAGE_BLOCKS).add(PalladiumItems.LEAD_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_LEAD_STORAGE_BLOCKS).add(PalladiumItems.RAW_LEAD_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_LEAD).add(PalladiumItems.RAW_LEAD.asItem());
        this.tag(PalladiumItemTags.LEAD_INGOTS).add(PalladiumItems.LEAD_INGOT.asItem());
        this.tag(PalladiumItemTags.LEAD_NUGGETS).add(PalladiumItems.LEAD_NUGGET.asItem());

        this.tag(PalladiumItemTags.TITANIUM_ORES).add(PalladiumItems.TITANIUM_ORE.get(), PalladiumItems.DEEPSLATE_TITANIUM_ORE.get(), PalladiumItems.METEORITE_TITANIUM_ORE.get());
        this.tag(PalladiumItemTags.TITANIUM_STORAGE_BLOCKS).add(PalladiumItems.TITANIUM_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_TITANIUM_STORAGE_BLOCKS).add(PalladiumItems.RAW_TITANIUM_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_TITANIUM).add(PalladiumItems.RAW_TITANIUM.asItem());
        this.tag(PalladiumItemTags.TITANIUM_INGOTS).add(PalladiumItems.TITANIUM_INGOT.asItem());
        this.tag(PalladiumItemTags.TITANIUM_NUGGETS).add(PalladiumItems.TITANIUM_NUGGET.asItem());

        this.tag(PalladiumItemTags.VIBRANIUM_ORES).add(PalladiumItems.VIBRANIUM_ORE.get(), PalladiumItems.DEEPSLATE_VIBRANIUM_ORE.get(), PalladiumItems.METEORITE_VIBRANIUM_ORE.get());
        this.tag(PalladiumItemTags.VIBRANIUM_STORAGE_BLOCKS).add(PalladiumItems.VIBRANIUM_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_VIBRANIUM_STORAGE_BLOCKS).add(PalladiumItems.RAW_VIBRANIUM_BLOCK.get());
        this.tag(PalladiumItemTags.RAW_VIBRANIUM).add(PalladiumItems.RAW_VIBRANIUM.asItem());
        this.tag(PalladiumItemTags.VIBRANIUM_INGOTS).add(PalladiumItems.VIBRANIUM_INGOT.asItem());
        this.tag(PalladiumItemTags.VIBRANIUM_NUGGETS).add(PalladiumItems.VIBRANIUM_NUGGET.asItem());
       
        
        this.tag(Tags.Items.ORES).addTags(PalladiumItemTags.LEAD_ORES, PalladiumItemTags.TITANIUM_ORES, PalladiumItemTags.VIBRANIUM_ORES);
        this.tag(Tags.Items.STORAGE_BLOCKS).addTags(
                PalladiumItemTags.LEAD_STORAGE_BLOCKS, PalladiumItemTags.RAW_LEAD_STORAGE_BLOCKS,
                PalladiumItemTags.TITANIUM_STORAGE_BLOCKS, PalladiumItemTags.RAW_TITANIUM_STORAGE_BLOCKS,
                PalladiumItemTags.VIBRANIUM_STORAGE_BLOCKS, PalladiumItemTags.RAW_VIBRANIUM_STORAGE_BLOCKS
        );
        this.tag(Tags.Items.RAW_MATERIALS).addTags(PalladiumItemTags.RAW_LEAD, PalladiumItemTags.RAW_TITANIUM, PalladiumItemTags.RAW_VIBRANIUM);
        this.tag(Tags.Items.INGOTS).addTags(PalladiumItemTags.LEAD_INGOTS, PalladiumItemTags.TITANIUM_INGOTS, PalladiumItemTags.VIBRANIUM_INGOTS);
        this.tag(Tags.Items.NUGGETS).addTags(PalladiumItemTags.LEAD_NUGGETS, PalladiumItemTags.TITANIUM_NUGGETS, PalladiumItemTags.VIBRANIUM_NUGGETS);

        this.tag(ItemTags.STONE_TOOL_MATERIALS).add(PalladiumItems.METEORITE_STONE.get());
        this.tag(ItemTags.STONE_CRAFTING_MATERIALS).add(PalladiumItems.METEORITE_STONE.get());

        this.tag(ItemTags.COAL_ORES).add(PalladiumItems.METEORITE_COAL_ORE.get());
        this.tag(ItemTags.IRON_ORES).add(PalladiumItems.METEORITE_IRON_ORE.get());
        this.tag(ItemTags.COPPER_ORES).add(PalladiumItems.METEORITE_COPPER_ORE.get());
        this.tag(ItemTags.GOLD_ORES).add(PalladiumItems.METEORITE_GOLD_ORE.get());
        this.tag(ItemTags.REDSTONE_ORES).add(PalladiumItems.METEORITE_REDSTONE_ORE.get());
        this.tag(ItemTags.EMERALD_ORES).add(PalladiumItems.METEORITE_EMERALD_ORE.get());
        this.tag(ItemTags.LAPIS_ORES).add(PalladiumItems.METEORITE_LAPIS_ORE.get());
        this.tag(ItemTags.DIAMOND_ORES).add(PalladiumItems.METEORITE_DIAMOND_ORE.get());
        this.tag(ItemTags.STAIRS).add(PalladiumItems.METEORITE_STONE_STAIRS.get(), PalladiumItems.METEORITE_BRICK_STAIRS.get());
        this.tag(ItemTags.WALLS).add(PalladiumItems.METEORITE_STONE_WALL.get(), PalladiumItems.METEORITE_BRICK_WALL.get());
        this.tag(ItemTags.SLABS).add(PalladiumItems.METEORITE_STONE_SLAB.get(), PalladiumItems.METEORITE_BRICK_SLAB.get());

        for (DyeColor color : DyeColor.values()) {
            this.tag(PalladiumItemTags.FABRIC_BY_COLOR.get(color)).add(PalladiumItems.FABRIC_BY_COLOR.get(color).get());
            this.tag(PalladiumItemTags.FABRICS).addTag(PalladiumItemTags.FABRIC_BY_COLOR.get(color));
            this.tag(PalladiumItemTags.DYED_BY_COLOR.get(color)).add(PalladiumItems.FABRIC_BY_COLOR.get(color).get());
        }
    }
}
