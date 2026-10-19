package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Effect {

    public List<EffectCondition> conditions = new ArrayList<>();

    public abstract void readSettings(JsonObject json);

}
