package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability.AbilityType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;

public class EffectConditionAbilityUnlocked extends EffectCondition {

    public String ability;
    public boolean useKey;

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        for (Ability ab : Ability.getAbilities(entity)) {
            if (this.useKey) {
                if (ab.getKey().equals(ability) && ab.isUnlocked()) {
                    return true;
                }
            } else {
                if (ab.getAbilityEntry().getRegistryName().toString().equals(ability) && ab.isUnlocked()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void readSettings(JsonObject json) {
        this.ability = JsonUtils.getString(json, "ability");
        this.useKey = JsonUtils.getBoolean(json, "use_key", false);
    }

}
