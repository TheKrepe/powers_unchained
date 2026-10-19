package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;

public class EffectConditionMoving extends EffectCondition {

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        return entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ;
    }

    @Override
    public void readSettings(JsonObject json) {

    }

}
