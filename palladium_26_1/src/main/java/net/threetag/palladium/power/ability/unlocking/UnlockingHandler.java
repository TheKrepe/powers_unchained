package net.threetag.palladium.power.ability.unlocking;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.registry.PalladiumRegistries;

import java.util.function.Function;

import java.util.List;

public abstract class UnlockingHandler {

    public static final Codec<UnlockingHandler> DIRECT_CODEC = PalladiumRegistries.ABILITY_UNLOCKING_HANDLER.byNameCodec().dispatch(UnlockingHandler::codec, Function.identity());

    public static final Codec<UnlockingHandler> CODEC = Codec.either(
            DIRECT_CODEC,
            Condition.CODEC
    ).xmap(
            either -> either.map(
                    left -> left,
                    ConditionalUnlockingHandler::new
            ),
            handler -> handler instanceof ConditionalUnlockingHandler cond ? Either.right(cond.condition) : Either.left(handler)
    );

    public boolean requiresContext() {
        return false;
    }

    public abstract boolean check(LivingEntity entity, AbilityInstance<?> abilityInstance, @Nullable DataContext context);

    public void registerDataComponents(DataComponentMap.Builder builder) {
    }

    /**
     * @return true if the click was handled by the handler, false to fall back to showing the ability's description
     */
    public boolean onClicked(Player player, AbilityInstance<?> abilityInstance) {
        return false;
    }

    public List<AbilityReference> getParentAbilities() {
        return List.of();
    }

    public abstract MapCodec<? extends UnlockingHandler> codec();

}
