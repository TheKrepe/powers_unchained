package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ForcedPerspectiveAbility extends Ability {

    public static final MapCodec<ForcedPerspectiveAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Perspective.CODEC.fieldOf("perspective").forGetter(ab -> ab.perspective),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, ForcedPerspectiveAbility::new));

    public final Perspective perspective;

    public ForcedPerspectiveAbility(Perspective perspective, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.perspective = perspective;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.FORCED_PERSPECTIVE.get();
    }

    public enum Perspective implements StringRepresentable {

        FIRST_PERSON("first_person"),
        THIRD_PERSON_BACK("third_person_back"),
        THIRD_PERSON_FRONT("third_person_front");

        public static final Codec<Perspective> CODEC = StringRepresentable.fromEnum(Perspective::values);

        private final String name;

        Perspective(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name;
        }
    }

    public static class Serializer extends AbilitySerializer<ForcedPerspectiveAbility> {

        @Override
        public MapCodec<ForcedPerspectiveAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ForcedPerspectiveAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Locks the camera of the player into a perspective while enabled. The perspective the player had before is restored afterwards.")
                    .add("perspective", SettingType.enumList(Perspective.values()), "The perspective the camera is locked into.")
                    .addExampleObject(new ForcedPerspectiveAbility(Perspective.THIRD_PERSON_FRONT, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}
