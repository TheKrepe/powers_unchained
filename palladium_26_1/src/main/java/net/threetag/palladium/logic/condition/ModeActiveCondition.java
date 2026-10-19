package net.threetag.palladium.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.ModeSwitchAbility;

import java.util.List;

public record ModeActiveCondition(AbilityReference ability, List<String> modes, boolean ignoreUnlocked) implements Condition {

    public static final MapCodec<ModeActiveCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AbilityReference.CODEC.fieldOf("ability").forGetter(ModeActiveCondition::ability),
            ExtraCodecs.compactListCodec(Codec.STRING).fieldOf("mode").forGetter(ModeActiveCondition::modes),
            Codec.BOOL.optionalFieldOf("ignore_unlocked", false).forGetter(ModeActiveCondition::ignoreUnlocked)
    ).apply(instance, ModeActiveCondition::new));

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        AbilityInstance<?> instance = this.ability.getInstance(entity, context.getPowerInstance());

        if (instance == null || !(instance.getAbility() instanceof ModeSwitchAbility) || (!this.ignoreUnlocked && !instance.isUnlocked())) {
            return false;
        }

        return this.modes.contains(ModeSwitchAbility.getActiveKey(instance));
    }

    @Override
    public ConditionSerializer<ModeActiveCondition> getSerializer() {
        return ConditionSerializers.MODE_ACTIVE.get();
    }

    public static class Serializer extends ConditionSerializer<ModeActiveCondition> {

        @Override
        public MapCodec<ModeActiveCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, ModeActiveCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Mode Active")
                    .setDescription("Checks if the active mode of a mode switch ability is one of the given mode keys. Returns false in any case if the given ability is locked.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The mode switch ability to check.")
                    .add("mode", TYPE_STRING, "A mode key, or a list of mode keys, any of which satisfies the condition.")
                    .add("ignore_unlocked", TYPE_BOOLEAN, "If true, the condition will check the active mode even if the ability is locked. Defaults to false.")
                    .addExampleObject(new ModeActiveCondition(new AbilityReference(null, "mode_switch"), List.of("mode_a", "mode_b"), false));
        }
    }
}
