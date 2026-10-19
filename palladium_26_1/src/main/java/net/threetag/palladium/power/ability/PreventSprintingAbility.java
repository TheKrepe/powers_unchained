package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventSprintingAbility extends Ability {

    public static final MapCodec<PreventSprintingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventSprintingAbility::new));

    public PreventSprintingAbility(AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<PreventSprintingAbility> getSerializer() {
        return AbilitySerializers.PREVENT_SPRINTING.get();
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && entity.isSprinting()) {
            entity.setSprinting(false);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    public static class Serializer extends AbilitySerializer<PreventSprintingAbility> {

        @Override
        public MapCodec<PreventSprintingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventSprintingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the player from sprinting.")
                    .addExampleObject(new PreventSprintingAbility(AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
