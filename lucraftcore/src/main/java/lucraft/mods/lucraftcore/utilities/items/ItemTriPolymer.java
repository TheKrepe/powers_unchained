package lucraft.mods.lucraftcore.utilities.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.item.EnumDyeColor;

public class ItemTriPolymer extends ItemBase {

    public ItemTriPolymer(EnumDyeColor color) {
        super("tri_polymer_" + StringHelper.unlocalizedToResourceName(color.getTranslationKey()));
        this.setCreativeTab(LucraftCore.CREATIVE_TAB);
    }

}
