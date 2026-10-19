package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;

import java.util.Arrays;
import java.util.List;

public record AsClientPlayerCondition(List<Condition> conditions) implements Condition {

    public static final MapCodec<AsClientPlayerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Condition.FALSE_TRUE_WRAPPED_CODEC.listOf().fieldOf("conditions").forGetter(AsClientPlayerCondition::conditions)
            ).apply(instance, AsClientPlayerCondition::new)
    );

    @Override
    public boolean test(DataContext context) {
        Player clientPlayer = Palladium.PROXY.getClientPlayer();

        if (clientPlayer == null) {
            return false;
        }

        DataContext clientContext = context.copy()
                .with(DataContextKeys.ENTITY, clientPlayer)
                .with(DataContextKeys.LEVEL, clientPlayer.level())
                .with(DataContextKeys.BLOCK_POS, clientPlayer.blockPosition())
                .with(DataContextKeys.EYE_SELECTION, EntityCustomizationHandler.get(clientPlayer).getEyeSelection());

        for (Condition condition : this.conditions) {
            if (!condition.test(clientContext)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ConditionSerializer<AsClientPlayerCondition> getSerializer() {
        return ConditionSerializers.AS_CLIENT_PLAYER.get();
    }

    public static class Serializer extends ConditionSerializer<AsClientPlayerCondition> {

        @Override
        public MapCodec<AsClientPlayerCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, AsClientPlayerCondition> builder, HolderLookup.Provider provider) {
            builder.setName("As Client Player")
                    .setDescription("Evaluates the given conditions against the client player (the player viewing the game) instead of the context entity. Useful for render layers that should only appear based on the viewing player's state. On the server, this condition always returns false.")
                    .add("conditions", TYPE_CONDITION_LIST, "List of conditions to evaluate against the client player")
                    .addExampleObject(new AsClientPlayerCondition(List.of(new CrouchingCondition())));
        }
    }
}
