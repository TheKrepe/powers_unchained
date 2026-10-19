package net.threetag.pymtech.tags;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.threetag.pymtech.PymTech;

public class PTItemTags {

    public static final Tags.IOptionalNamedTag<Item> PYM_SUITS = get("pym_suits");

    public static final Tags.IOptionalNamedTag<Item> ANT_MAN_SUITS = get("ant_man_suits");
    public static final Tags.IOptionalNamedTag<Item> ANT_MAN_SUIT = get("suits/ant_man");
    public static final Tags.IOptionalNamedTag<Item> ANT_MAN_TIER2_SUIT = get("suits/ant_man_tier2");
    public static final Tags.IOptionalNamedTag<Item> ANT_MAN_TIER3_SUIT = get("suits/ant_man_tier3");
    public static final Tags.IOptionalNamedTag<Item> GIANT_MAN_SUIT = get("suits/giant_man");
    public static final Tags.IOptionalNamedTag<Item> ZOMBIE_GIANT_MAN_SUIT = get("suits/zombie_giant_man");

    public static final Tags.IOptionalNamedTag<Item> WASP_SUITS = get("wasp_suits");
    public static final Tags.IOptionalNamedTag<Item> WASP_SUIT = get("suits/wasp");
    public static final Tags.IOptionalNamedTag<Item> WASP_TIER2_SUIT = get("suits/wasp_tier2");

    public static final Tags.IOptionalNamedTag<Item> REGULATORS = get("regulators");
    public static final Tags.IOptionalNamedTag<Item> REGULATOR_VIALS = get("regulator_vials");

    public static final Tags.IOptionalNamedTag<Item> SHRINK_DISKS = get("shrink_disks");
    public static final Tags.IOptionalNamedTag<Item> ENLARGE_DISKS = get("enlarge_disks");

    private static Tags.IOptionalNamedTag<Item> get(String name) {
        return get(PymTech.MODID, name);
    }

    private static Tags.IOptionalNamedTag<Item> get(String namespace, String name) {
        return ItemTags.createOptional(new ResourceLocation(namespace, name));
    }

}
