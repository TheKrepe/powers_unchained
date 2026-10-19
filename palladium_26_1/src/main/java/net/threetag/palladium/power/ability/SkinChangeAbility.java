package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.entity.SkinTypedValue;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;
import java.util.Optional;

public class SkinChangeAbility extends Ability {

    public static final MapCodec<SkinChangeAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SkinTypedValue.codec(TextureReference.CODEC).fieldOf("texture").forGetter(ab -> ab.texture),
                    PlayerModelType.CODEC.optionalFieldOf("model_type").forGetter(ab -> ab.modelChangeType),
                    Codec.INT.optionalFieldOf("priority", 50).forGetter(ab -> ab.priority),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, SkinChangeAbility::new));

    public final SkinTypedValue<TextureReference> texture;
    public final Optional<PlayerModelType> modelChangeType;
    public final int priority;

    public SkinChangeAbility(SkinTypedValue<TextureReference> texture, Optional<PlayerModelType> modelChangeType, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.texture = texture;
        this.modelChangeType = modelChangeType;
        this.priority = priority;
    }

    @Override
    public AbilitySerializer<SkinChangeAbility> getSerializer() {
        return AbilitySerializers.SKIN_CHANGE.get();
    }

    public static class Serializer extends AbilitySerializer<SkinChangeAbility> {

        @Override
        public MapCodec<SkinChangeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, SkinChangeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("An ability that changes the player's skin.")
                    .add("texture", TYPE_TEXTURE_REFERENCE, "The texture that should be used for the player's skin.")
                    .addOptional("model_type", SettingType.enumList(PlayerModelType.values()), "Model type for the player. If left empty it will keep the player's model type. 'wide' = Wide-armed Steve model; 'slim' = Slim-armed Alex model;")
                    .addOptional("priority", TYPE_INT, "Priority for the skin (in case multiple skin changes are applied, the one with the highest priority will be used)", 50)
                    .addExampleObject(new SkinChangeAbility(new SkinTypedValue<>(TextureReference.normal(Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png"))), Optional.empty(), 50, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new SkinChangeAbility(new SkinTypedValue<>(TextureReference.normal(Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png"))), Optional.of(PlayerModelType.WIDE), 50, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
