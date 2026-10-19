package net.threetag.palladium.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.ModeSwitchAbility;

public class SetModeAction extends Action {

    public static final MapCodec<SetModeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AbilityReference.CODEC.fieldOf("ability").forGetter(a -> a.ability),
            Codec.STRING.fieldOf("mode").forGetter(a -> a.mode)
    ).apply(instance, SetModeAction::new));

    private final AbilityReference ability;
    private final String mode;

    public SetModeAction(AbilityReference ability, String mode) {
        this.ability = ability;
        this.mode = mode;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null) {
            return false;
        }

        var abilityInstance = this.ability.getInstance(entity, context.getPowerInstance());

        if (abilityInstance == null) {
            return false;
        }

        return ModeSwitchAbility.setMode(entity, abilityInstance, this.mode);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.SET_MODE.get();
    }

    public static class Serializer extends ActionSerializer<SetModeAction> {

        @Override
        public MapCodec<SetModeAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetModeAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Mode")
                    .setDescription("Forces a mode switch ability into a specific mode, ignoring that mode's conditions.")
                    .add("ability", TYPE_ABILITY_REFERENCE, "The mode switch ability to change.")
                    .add("mode", TYPE_STRING, "The key of the mode the ability is set to.")
                    .addExampleObject(new SetModeAction(new AbilityReference(
                            Identifier.fromNamespaceAndPath("example", "power"),
                            "ability_key"
                    ), "mode_a"));
        }
    }
}
