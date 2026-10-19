package lucraft.mods.heroesexpansion.conditions;

import lucraft.mods.heroesexpansion.abilities.AbilitySolarEnergy;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityCondition;
import net.minecraft.util.text.TextComponentTranslation;

public class AbilityConditionSolarEnergy extends AbilityCondition {

    public AbilityConditionSolarEnergy(AbilitySolarEnergy abilitySolarEnergy, int minimum) {
        super((a) -> abilitySolarEnergy.getDataManager().get(AbilitySolarEnergy.SOLAR_ENERGY) >= minimum, new TextComponentTranslation("heroesexpansion.ability.condition.solar_energy", minimum));
    }
}
