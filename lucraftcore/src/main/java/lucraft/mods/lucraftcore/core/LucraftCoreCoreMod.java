package lucraft.mods.lucraftcore.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({"lucraft.mods.lucraftcore.core"})
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class LucraftCoreCoreMod implements IFMLLoadingPlugin {

    public static boolean runtimeObfuscationEnabled;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{LCTransformer.class.getCanonicalName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        runtimeObfuscationEnabled = !((Boolean) data.get("runtimeDeobfuscationEnabled")).booleanValue();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
