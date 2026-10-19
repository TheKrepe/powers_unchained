package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public class EffectConditionAlways extends EffectCondition {

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        return true;
    }

    @Override
    public void readSettings(JsonObject json) {

    }

}
