package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability.AbilityType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class EffectConditionAbilityEnabled extends EffectCondition {

    public String ability_;
    // Need to keep this here to not break compatibility
    public ResourceLocation ability;
    public boolean useKey;

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        for (Ability ab : Ability.getAbilities(entity)) {
            if(this.useKey) {
                if (ab.getKey().equals(this.getName()) && ab.isUnlocked()) {
                    if (ab.getAbilityType() == AbilityType.CONSTANT)
                        return true;
                    if (ab.isEnabled())
                        return true;
                }
            } else {
                if (ab.getAbilityEntry().getRegistryName().toString().equals(this.getName()) && ab.isUnlocked()) {
                    if (ab.getAbilityType() == AbilityType.CONSTANT)
                        return true;
                    if (ab.isEnabled())
                        return true;
                }
            }
        }
        return false;
    }

    private String getName() {
        return this.ability != null ? this.ability.toString() : this.ability_;
    }

    @Override
    public void readSettings(JsonObject json) {
        this.ability_ = JsonUtils.getString(json, "ability");
        this.useKey = JsonUtils.getBoolean(json, "use_key", false);
    }

}
