package net.threetag.palladium.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.client.gui.shake.ScreenShake;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.Collections;
import java.util.List;

public class ScreenShakeAbility extends Ability {

    private static final FloatValueHolder DEFAULT_FREQUENCY = new FloatValueHolder(0.3F);

    public static final MapCodec<ScreenShakeAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ScreenShake.ShakeIntensity.CODEC.fieldOf("intensity").forGetter(ab -> ab.intensity),
                    FloatValueHolder.CODEC.optionalFieldOf("frequency", DEFAULT_FREQUENCY).forGetter(ab -> ab.frequency),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, ScreenShakeAbility::new));

    public final ScreenShake.ShakeIntensity intensity;
    public final FloatValueHolder frequency;

    public ScreenShakeAbility(ScreenShake.ShakeIntensity intensity, FloatValueHolder frequency, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.intensity = intensity;
        this.frequency = frequency;
    }

    @Override
    public AbilitySerializer<ScreenShakeAbility> getSerializer() {
        return AbilitySerializers.SCREEN_SHAKE.get();
    }

    public static class Serializer extends AbilitySerializer<ScreenShakeAbility> {

        @Override
        public MapCodec<ScreenShakeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ScreenShakeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Shakes the camera of the player who has the ability while it is enabled. The strength follows the ability's animation timer, so setting an \"animation_timer\" in the properties makes the shake fade in and out smoothly. Without one, the shake starts and stops instantly.")
                    .add("intensity", SettingType.combined(TYPE_FLOAT, SettingType.simple("Shake Intensity (yaw, pitch, roll)")), "How far the camera moves, in degrees. A single number shakes yaw and pitch by that amount, an object sets yaw, pitch and roll separately.")
                    .addOptional("frequency", TYPE_FLOAT_VALUE, "How many shake cycles happen per tick. Higher values shake faster. Can be a dynamic value, the shake then speeds up or slows down smoothly.", 0.3F)
                    .addExampleObject(new ScreenShakeAbility(new ScreenShake.ShakeIntensity(1.5F, 1.5F, 0.5F), DEFAULT_FREQUENCY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }
}
