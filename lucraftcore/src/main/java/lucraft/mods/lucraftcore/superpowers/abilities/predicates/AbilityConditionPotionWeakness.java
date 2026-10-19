package lucraft.mods.lucraftcore.superpowers.abilities.predicates;

import net.minecraft.potion.Potion;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * Created by Nictogen on 2019-02-17.
 */
public class AbilityConditionPotionWeakness extends AbilityCondition {

    public Potion potion;

    public AbilityConditionPotionWeakness(Potion potion) {
        super(ability -> !ability.getEntity().isPotionActive(potion), new TextComponentTranslation("lucraftcore.ability.condition.potion_weakness", new TextComponentTranslation(potion.getName())));
        this.potion = potion;
    }
}
