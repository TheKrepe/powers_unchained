package lucraft.mods.lucraftcore.core;

import lucraft.mods.lucraftcore.sizechanging.ModuleSizeChanging;
import lucraft.mods.lucraftcore.sizechanging.capabilities.CapabilitySizeChanging;
import net.minecraft.entity.Entity;

public class LCHooks {

    public static void setSize(Entity entity, float width, float height) {
        if (ModuleSizeChanging.INSTANCE.isEnabled() && entity.hasCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null))
            entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING_CAP, null).setOriginalSize(width, height);
    }

}
