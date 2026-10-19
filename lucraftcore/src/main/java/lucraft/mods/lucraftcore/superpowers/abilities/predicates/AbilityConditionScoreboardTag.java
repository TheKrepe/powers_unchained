package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import net.minecraft.util.text.TextComponentTranslation;

import java.util.function.Predicate;

public class AbilityConditionScoreboardTag extends AbilityCondition {

    public AbilityConditionScoreboardTag(String tag) {
        super((a) -> {
            for(String s : a.getEntity().getTags()) {
                if(s.equals(tag)) {
                    return true;
                }
            }
            return false;
        }, new TextComponentTranslation("lucraftcore.ability.condition.scoreboard_tag", tag));
    }

}
