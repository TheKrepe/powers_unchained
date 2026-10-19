package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public abstract class EffectCondition {

    public abstract boolean isFulFilled(EntityLivingBase entity);

    public abstract void readSettings(JsonObject json);

}
