package lucraft.mods.lucraftcore.util.creativetabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CreativeTabRegistry {

    private static Map<String, CreativeTabs> TABS = new HashMap<>();

    public static CreativeTabs getCreativeTab(String name) {
        if (name.equalsIgnoreCase("blocks"))
            return CreativeTabs.BUILDING_BLOCKS;
        if (name.equalsIgnoreCase("decoration"))
            return CreativeTabs.DECORATIONS;
        if (name.equalsIgnoreCase("redstone"))
            return CreativeTabs.REDSTONE;
        if (name.equalsIgnoreCase("transportation"))
            return CreativeTabs.TRANSPORTATION;
        if (name.equalsIgnoreCase("misc"))
            return CreativeTabs.MISC;
        if (name.equalsIgnoreCase("food"))
            return CreativeTabs.FOOD;
        if (name.equalsIgnoreCase("tools"))
            return CreativeTabs.TOOLS;
        if (name.equalsIgnoreCase("combat"))
            return CreativeTabs.COMBAT;
        if (name.equalsIgnoreCase("brewing"))
            return CreativeTabs.BREWING;

        return TABS.get(name.toLowerCase());
    }

    public static CreativeTabs addCreativeTab(String name, ItemStack stack) {
        CreativeTabs tab = new CreativeTabs(name) {
            @Override
            public ItemStack createIcon() {
                return stack;
            }
        };
        TABS.put(name, tab);
        return tab;
    }

    public static CreativeTabs addCreativeTab(String name, CreativeTabs tab) {
        TABS.put(name, tab);
        return tab;
    }

    public static CreativeTabs getOrCreateCreativeTab(String name, ItemStack stack) {
        CreativeTabs tab = getCreativeTab(name);

        if (tab != null)
            return tab;
        else {
            tab = addCreativeTab(name, stack);
            return tab;
        }
    }

}
