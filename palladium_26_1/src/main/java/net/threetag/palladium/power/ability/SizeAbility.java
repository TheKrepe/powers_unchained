package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.Collections;
import java.util.List;

public class SizeAbility extends Ability {

    public static final MapCodec<SizeAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    PalladiumCodecs.POSITIVE_FLOAT_DYNAMIC.fieldOf("size").forGetter(ab -> ab.size),
                    RegistryFixedCodec.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE).fieldOf("size_change_type").forGetter(ab -> ab.sizeChangeType),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, SizeAbility::new));

    public final FloatValueHolder size;
    public final Holder<SizeChangeType> sizeChangeType;

    public SizeAbility(FloatValueHolder size, Holder<SizeChangeType> sizeChangeType, AbilityProperties properties,
                       AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.size = size;
        this.sizeChangeType = sizeChangeType;
    }

    @Override
    public AbilitySerializer<SizeAbility> getSerializer() {
        return AbilitySerializers.SIZE.get();
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        this.applySize(entity, abilityInstance);
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && this.size.isDynamic()) {
            this.applySize(entity, abilityInstance);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    private void applySize(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide()) {
            return;
        }

        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());

        if (sizeData == null) {
            return;
        }

        sizeData.applyModifier(this.sizeChangeType, this.size.get(DataContext.forAbility(entity, abilityInstance)));
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> instance) {
        if (entity.level().isClientSide()) {
            return;
        }

        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        if (sizeData == null) {
            return;
        }

        sizeData.removeModifier(this.sizeChangeType);
    }

    public static class Serializer extends AbilitySerializer<SizeAbility> {

        @Override
        public MapCodec<SizeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, SizeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Changes the size of the entity while the ability is enabled, smoothly reverting when disabled.")
                    .add("size", TYPE_FLOAT_VALUE, "The target size multiplier (multiplicative with other active size modifiers). Clamped to the size change type's min/max size.")
                    .add("size_change_type", TYPE_IDENTIFIER, "The size change type defining the transition duration and easing.")
                    .addExampleObject(new SizeAbility(
                            new FloatValueHolder(2F), provider.holderOrThrow(PalladiumSizeChangeTypes.DEFAULT),
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList())
                    );
        }
    }
}
