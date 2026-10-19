package lucraft.mods.heroesexpansion.conditions;

import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityCondition;
import net.minecraft.util.text.TextComponentTranslation;

public class AbilityConditionThorWeapon extends AbilityCondition {

    public AbilityConditionThorWeapon() {
        super((a) -> a.getEntity().getHeldItemMainhand().getItem() instanceof ItemThorWeapon || a.getEntity().getHeldItemOffhand().getItem() instanceof ItemThorWeapon, new TextComponentTranslation("heroesexpansion.ability.condition.thor_weapon"));
    }
}
