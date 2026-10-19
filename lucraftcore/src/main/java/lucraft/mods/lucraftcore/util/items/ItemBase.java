package lucraft.mods.lucraftcore.util.items;

import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBase extends Item {

    public String name;

    public ItemBase(String name) {
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.name = name;
        this.setCreativeTab(CreativeTabs.MISC);
    }

}
