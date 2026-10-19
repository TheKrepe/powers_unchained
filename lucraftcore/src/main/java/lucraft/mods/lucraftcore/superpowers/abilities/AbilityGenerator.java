package lucraft.mods.lucraftcore.superpowers.abilities;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AbilityGenerator {

    public ResourceLocation loc;
    public String key;
    public JsonObject data;

    public AbilityGenerator(ResourceLocation loc, String key, JsonObject data) {
        this.loc = loc;
        this.key = key;
        this.data = data;
    }

    public Ability create(EntityLivingBase entity, Ability.AbilityMap abilities) {
        Ability ab = null;
        try {
            if (Ability.ABILITY_REGISTRY.containsKey(loc)) {
                ab = Ability.ABILITY_REGISTRY.getValue(loc).getAbilityClass().getConstructor(EntityLivingBase.class).newInstance(entity);
                ab.readFromAddonPack(data, abilities);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return ab;
    }

}
