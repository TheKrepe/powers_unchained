package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;

public class EffectConditionNot extends EffectCondition {

    public List<EffectCondition> conditions = new ArrayList<>();

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        boolean b = true;
        for (EffectCondition effectCondition : conditions) {
            if (!effectCondition.isFulFilled(entity)) {
                b = false;
            }
        }
        return !b;
    }

    @Override
    public void readSettings(JsonObject json) {
        if (JsonUtils.hasField(json, "conditions")) {
            JsonArray conArray = JsonUtils.getJsonArray(json, "conditions");
            for (int i = 0; i < conArray.size(); i++) {
                JsonObject condition = conArray.get(i).getAsJsonObject();
                String conType = JsonUtils.getString(condition, "type");
                if (!EffectHandler.CONDITIONS.containsKey(conType))
                    try {
                        throw new Exception("The effect condition '" + conType + "' doesn't exist!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                EffectCondition c = null;
                try {
                    c = EffectHandler.CONDITIONS.get(conType).newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                c.readSettings(condition);
                conditions.add(c);
            }
        } else {
            conditions.add(new EffectConditionAlways());
        }
    }
}
