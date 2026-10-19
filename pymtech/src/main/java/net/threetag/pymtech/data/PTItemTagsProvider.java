package net.threetag.pymtech.data;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.threetag.pymtech.item.PTItems;
import net.threetag.pymtech.tags.PTItemTags;
import net.threetag.threecore.item.TCItems;

public class PTItemTagsProvider extends ItemTagsProvider {

    public PTItemTagsProvider(DataGenerator generatorIn, BlockTagsProvider blockTagsProvider) {
        super(generatorIn, blockTagsProvider);
    }

    @Override
    protected void registerTags() {
        this.getOrCreateBuilder(PTItemTags.ANT_MAN_SUIT).add(PTItems.Suits.ANT_MAN_HELMET, PTItems.Suits.ANT_MAN_CHESTPLATE, PTItems.Suits.ANT_MAN_LEGGINGS, PTItems.Suits.ANT_MAN_BOOTS);
        this.getOrCreateBuilder(PTItemTags.ANT_MAN_TIER2_SUIT).add(PTItems.Suits.ANT_MAN_TIER2_HELMET, PTItems.Suits.ANT_MAN_TIER2_CHESTPLATE, PTItems.Suits.ANT_MAN_TIER2_LEGGINGS, PTItems.Suits.ANT_MAN_TIER2_BOOTS);
        this.getOrCreateBuilder(PTItemTags.ANT_MAN_TIER3_SUIT).add(PTItems.Suits.ANT_MAN_TIER3_HELMET, PTItems.Suits.ANT_MAN_TIER3_CHESTPLATE, PTItems.Suits.ANT_MAN_TIER3_LEGGINGS, PTItems.Suits.ANT_MAN_TIER3_BOOTS);
        this.getOrCreateBuilder(PTItemTags.GIANT_MAN_SUIT).add(PTItems.Suits.GIANT_MAN_HELMET, PTItems.Suits.GIANT_MAN_CHESTPLATE, PTItems.Suits.GIANT_MAN_LEGGINGS, PTItems.Suits.GIANT_MAN_BOOTS);
        this.getOrCreateBuilder(PTItemTags.ZOMBIE_GIANT_MAN_SUIT).add(PTItems.Suits.ZOMBIE_GIANT_MAN_HELMET, PTItems.Suits.ZOMBIE_GIANT_MAN_CHESTPLATE, PTItems.Suits.ZOMBIE_GIANT_MAN_LEGGINGS, PTItems.Suits.ZOMBIE_GIANT_MAN_BOOTS);
        this.getOrCreateBuilder(PTItemTags.ANT_MAN_SUITS).addTag(PTItemTags.ANT_MAN_SUIT).addTag(PTItemTags.ANT_MAN_TIER2_SUIT).addTag(PTItemTags.ANT_MAN_TIER3_SUIT).addTag(PTItemTags.GIANT_MAN_SUIT).addTag(PTItemTags.ZOMBIE_GIANT_MAN_SUIT);

        this.getOrCreateBuilder(PTItemTags.WASP_SUIT).add(PTItems.Suits.WASP_HELMET, PTItems.Suits.WASP_CHESTPLATE, PTItems.Suits.WASP_LEGGINGS, PTItems.Suits.WASP_BOOTS);
        this.getOrCreateBuilder(PTItemTags.WASP_TIER2_SUIT).add(PTItems.Suits.WASP_TIER2_HELMET, PTItems.Suits.WASP_TIER2_CHESTPLATE, PTItems.Suits.WASP_TIER2_LEGGINGS, PTItems.Suits.WASP_TIER2_BOOTS);
        this.getOrCreateBuilder(PTItemTags.WASP_SUITS).addTag(PTItemTags.WASP_SUIT).addTag(PTItemTags.WASP_TIER2_SUIT);

        this.getOrCreateBuilder(PTItemTags.PYM_SUITS).addTag(PTItemTags.ANT_MAN_SUITS).addTag(PTItemTags.WASP_SUITS);

        this.getOrCreateBuilder(PTItemTags.REGULATORS).add(PTItems.REGULATOR.get());
        this.getOrCreateBuilder(PTItemTags.REGULATOR_VIALS).add(TCItems.VIAL.get());

        this.getOrCreateBuilder(PTItemTags.SHRINK_DISKS).add(PTItems.SHRINK_DISK.get());
        this.getOrCreateBuilder(PTItemTags.ENLARGE_DISKS).add(PTItems.ENLARGE_DISK.get());
    }

    @Override
    public String getName() {
        return "PymTech Item Tags";
    }

}
