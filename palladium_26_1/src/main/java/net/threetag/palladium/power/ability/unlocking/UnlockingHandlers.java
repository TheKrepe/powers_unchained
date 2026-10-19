package net.threetag.palladium.power.ability.unlocking;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class UnlockingHandlers {

    public static final DeferredRegister<MapCodec<? extends UnlockingHandler>> UNLOCKING_HANDLERS = DeferredRegister.create(PalladiumRegistryKeys.ABILITY_UNLOCKING_HANDLER, Palladium.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends UnlockingHandler>, MapCodec<ConditionalUnlockingHandler>> CONDITIONAL = UNLOCKING_HANDLERS.register("conditional", () -> ConditionalUnlockingHandler.CODEC);
    public static final DeferredHolder<MapCodec<? extends UnlockingHandler>, MapCodec<BuyableUnlockingHandler>> BUYABLE = UNLOCKING_HANDLERS.register("buyable", () -> BuyableUnlockingHandler.CODEC);

}
