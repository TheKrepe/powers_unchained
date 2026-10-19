package net.threetag.pymtech.ability;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.threetag.pymtech.PymTech;
import net.threetag.threecore.ability.AbilityType;

public class PTAbilityTypes {

    public static final DeferredRegister<AbilityType> ABILITY_TYPES = DeferredRegister.create(AbilityType.class, PymTech.MODID);

    public static final RegistryObject<AbilityType> REGULATOR_TIER1 = ABILITY_TYPES.register("regulator", () -> new AbilityType(RegulatorTier1Ability::new));
    public static final RegistryObject<AbilityType> REGULATOR_TIER2 = ABILITY_TYPES.register("regulator_tier2", () -> new AbilityType(RegulatorTier2Ability::new));
    public static final RegistryObject<AbilityType> REGULATOR_TIER3 = ABILITY_TYPES.register("regulator_tier3", () -> new AbilityType(RegulatorTier3Ability::new));
    public static final RegistryObject<AbilityType> PYM_PARTICLE_SIZE_CHANGE = ABILITY_TYPES.register("pym_particle_size_change", () -> new AbilityType(PymParticleSizeChangeAbility::new));

}
