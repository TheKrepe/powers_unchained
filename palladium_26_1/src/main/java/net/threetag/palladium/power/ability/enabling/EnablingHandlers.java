package net.threetag.palladium.power.ability.enabling;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class EnablingHandlers {

    public static final DeferredRegister<MapCodec<? extends EnablingHandler>> ENABLING_HANDLERS = DeferredRegister.create(PalladiumRegistryKeys.ABILITY_ENABLING_HANDLER, Palladium.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends EnablingHandler>, MapCodec<ConditionalEnablingHandler>> CONDITIONAL = ENABLING_HANDLERS.register("conditional", () -> ConditionalEnablingHandler.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnablingHandler>, MapCodec<KeyBindEnablingHandler>> KEY_BIND = ENABLING_HANDLERS.register("key_bind", () -> KeyBindEnablingHandler.CODEC);

}
