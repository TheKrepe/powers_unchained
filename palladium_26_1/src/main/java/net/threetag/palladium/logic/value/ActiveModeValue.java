package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.ModeSwitchAbility;

public class ActiveModeValue extends StringValue {

    public static final MapCodec<ActiveModeValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AbilityReference.CODEC.fieldOf("ability").forGetter(v -> v.ability),
            modifyFunctionCodec()
    ).apply(instance, ActiveModeValue::new));

    private final AbilityReference ability;

    public ActiveModeValue(AbilityReference ability, String molang) {
        super(molang);
        this.ability = ability;
    }

    @Override
    public String getString(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return "";
        }

        AbilityInstance<?> instance = this.ability.getInstance(entity, context.getPowerInstance());
        return instance != null ? ModeSwitchAbility.getActiveKey(instance) : "";
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.ACTIVE_MODE.get();
    }

    public static class Serializer extends StringSerializer<ActiveModeValue> {

        @Override
        public MapCodec<ActiveModeValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, ActiveModeValue> builder, HolderLookup.Provider provider) {
            builder.setName("Active Mode").setDescription("Returns the key of the active mode of a mode switch ability.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The mode switch ability to read the active mode from.")
                    .addExampleObject(new ActiveModeValue(new AbilityReference(null, "mode_switch"), ""));
        }
    }
}
