package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;

import java.util.List;

public interface IAbilityConditionFactory {

    AbilityCondition parse(JsonObject json, Ability ability, Ability.AbilityMap abilities);

}
