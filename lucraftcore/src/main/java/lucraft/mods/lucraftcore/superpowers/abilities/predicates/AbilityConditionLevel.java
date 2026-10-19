package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainerSuperpower;
import net.minecraft.util.text.TextComponentTranslation;

public class AbilityConditionLevel extends AbilityCondition {

    public AbilityConditionLevel(int level) {
        super((a) -> {
                    AbilityContainer container = Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, a.getEntity());
                    AbilityContainerSuperpower handler = container instanceof AbilityContainerSuperpower ? (AbilityContainerSuperpower) container : null;
                    return SuperpowerHandler.hasSuperpower(a.getEntity()) && SuperpowerHandler.getSuperpower(a.getEntity()).canLevelUp() && handler != null && handler.getLevel() >= level;
                },
                new TextComponentTranslation("lucraftcore.ability.condition.level", level));
    }
}
