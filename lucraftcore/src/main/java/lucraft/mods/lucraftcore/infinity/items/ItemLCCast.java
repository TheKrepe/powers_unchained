package lucraft.mods.lucraftcore.infinity.items;

import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraftforge.fml.common.Optional;
import slimeknights.tconstruct.library.smeltery.ICast;

@Optional.Interface(iface = "slimeknights.tconstruct.library.smeltery.ICast", modid = "tconstruct", striprefs = false)
public class ItemLCCast extends ItemBase implements ICast {

    public ItemLCCast(String name) {
        super(name);
    }

}
