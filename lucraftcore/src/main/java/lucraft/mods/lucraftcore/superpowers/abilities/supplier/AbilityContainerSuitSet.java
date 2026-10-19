package lucraft.mods.lucraftcore.superpowers.abilities.supplier;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.entity.EntityLivingBase;

public class AbilityContainerSuitSet extends AbilityContainer {

    public AbilityContainerSuitSet(EntityLivingBase entity, Ability.EnumAbilityContext context) {
        super(entity, context);
    }

    @Override
    public void switchProvider(IAbilityProvider provider) {
        if (this.provider instanceof SuitSet)
            ((SuitSet) this.provider).onUnequip((SuitSet) this.provider, this.entity);
        super.switchProvider(provider);
        if (provider instanceof SuitSet)
            ((SuitSet) provider).onEquip((SuitSet) provider, this.entity);
    }
}
