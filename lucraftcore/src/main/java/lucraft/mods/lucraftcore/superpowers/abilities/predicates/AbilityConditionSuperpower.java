package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.util.text.TextComponentTranslation;

public class AbilityConditionSuperpower extends AbilityCondition {

    public AbilityConditionSuperpower(Superpower superpower) {
        super((a) -> (SuperpowerHandler.hasSuperpower(a.getEntity(), superpower)), new TextComponentTranslation("lucraftcore.ability.condition.superpower", superpower.getDisplayName()));
    }

}
