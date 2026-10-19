package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.util.text.TextComponentTranslation;

public class AbilityConditionAbility extends AbilityCondition {

    public AbilityConditionAbility(Ability ability) {
        super((a) -> (ability.isUnlocked() && ability.isEnabled()), new TextComponentTranslation("lucraftcore.ability.condition.ability", ability.getDisplayName()));
    }
}
