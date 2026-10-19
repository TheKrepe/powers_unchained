package lucraft.mods.lucraftcore.util.render;

import net.minecraft.client.model.ModelBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class ModelCache {

    private static Map<String, ModelBase> CACHE = new HashMap<>();

    public static ModelBase getModel(String key) {
        return CACHE.getOrDefault(key, null);
    }

    public static ModelBase storeModel(String key, ModelBase model) {
        CACHE.put(key, model);
        return model;
    }

    public static ModelBase getOrStoreModel(String key, Supplier<ModelBase> modelSupplier) {
        if(CACHE.containsKey(key)) {
            return CACHE.get(key);
        } else {
            ModelBase model = modelSupplier.get();
            CACHE.put(key, model);
            return model;
        }
    }

}
