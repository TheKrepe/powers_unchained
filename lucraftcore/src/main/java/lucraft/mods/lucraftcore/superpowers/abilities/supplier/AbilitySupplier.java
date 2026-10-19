package lucraft.mods.lucraftcore.superpowers.abilities.supplier;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

public class AbilitySupplier {

    public Function<EntityLivingBase, IAbilityProvider> providerSupplier;
    public AbilitySupplier.AbilityContainerFactory containerFactory;

    public AbilitySupplier(Function<EntityLivingBase, IAbilityProvider> providerSupplier, AbilitySupplier.AbilityContainerFactory containerFactory) {
        this.providerSupplier = providerSupplier;
        this.containerFactory = containerFactory;
    }

    public interface AbilityContainerFactory {

        AbilityContainer create(EntityLivingBase player, Ability.EnumAbilityContext context);

    }

}
