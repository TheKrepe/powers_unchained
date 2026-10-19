package net.threetag.palladium.power.ability.enabling;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.registry.PalladiumRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class EnablingHandler {

    public static final Codec<EnablingHandler> DIRECT_CODEC = PalladiumRegistries.ABILITY_ENABLING_HANDLER.byNameCodec().dispatch(EnablingHandler::codec, Function.identity());

    public static final Codec<EnablingHandler> CODEC = Codec.either(
            DIRECT_CODEC,
            Condition.CODEC
    ).xmap(
            either -> either.map(
                    left -> left,
                    ConditionalEnablingHandler::new
            ),
            handler -> handler instanceof ConditionalEnablingHandler cond ? Either.right(cond.condition) : Either.left(handler)
    );

    public boolean requiresContext() {
        return false;
    }

    public abstract boolean check(LivingEntity entity, AbilityInstance<?> abilityInstance, @Nullable DataContext context);

    public void tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
    }

    public void registerDataComponents(DataComponentMap.Builder builder) {
    }

    public void onUnlocked(LivingEntity entity, AbilityInstance<?> abilityInstance) {
    }

    public boolean isKeyBound() {
        return false;
    }

    public abstract MapCodec<? extends EnablingHandler> codec();

}
