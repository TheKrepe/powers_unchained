package lucraft.mods.lucraftcore.addonpacks;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraftforge.common.config.Config;

@Config(modid = LucraftCore.MODID, name = "lc_addonpacks")
public class AddonPackConfig {

    @Config.RequiresMcRestart
    public static String addonpacksFolder = "addonpacks";

}
