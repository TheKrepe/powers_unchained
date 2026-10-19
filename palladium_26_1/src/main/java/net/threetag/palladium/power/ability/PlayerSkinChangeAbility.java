package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.StringDataAttachmentValue;
import net.threetag.palladium.logic.value.StringValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.logic.value.holder.StringValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PlayerSkinChangeAbility extends Ability {

    public static final MapCodec<PlayerSkinChangeAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    StringValueHolder.CODEC.fieldOf("username").forGetter(ab -> ab.username),
                    Codec.INT.optionalFieldOf("priority", 50).forGetter(ab -> ab.priority),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, PlayerSkinChangeAbility::new));

    public final StringValueHolder username;
    public final int priority;

    public PlayerSkinChangeAbility(StringValueHolder username, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.username = username;
        this.priority = priority;
    }

    @Override
    public AbilitySerializer<PlayerSkinChangeAbility> getSerializer() {
        return AbilitySerializers.PLAYER_SKIN_CHANGE.get();
    }

    public static class Serializer extends AbilitySerializer<PlayerSkinChangeAbility> {

        @Override
        public MapCodec<PlayerSkinChangeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PlayerSkinChangeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("An ability that changes the player's skin to the one of a Minecraft player one.")
                    .add("username", TYPE_STRING_VALUE, "The username of the player you want to turn into.")
                    .addOptional("priority", TYPE_INT, "Priority for the skin (in case multiple skin changes are applied, the one with the highest priority will be used)", 50)
                    .addExampleObject(new PlayerSkinChangeAbility(new StringValueHolder("Lucraaaft"), 50, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PlayerSkinChangeAbility(new StringValueHolder(new StringDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "data_attachment_id")), "", "")), 50, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
