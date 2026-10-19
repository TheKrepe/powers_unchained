package net.threetag.palladium.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;

public class PalladiumSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Palladium.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_FLIGHT_WEB_SHOOT = make("player.flight.web_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_FLIGHT_PROPELLED = make("player.flight.propelled");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_FLIGHT_SWING = make("player.flight.swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> MULTIVERSE_SEARCH = make("generic.multiverse_search");
    public static final DeferredHolder<SoundEvent, SoundEvent> MULTIVERSAL_VARIANT_CRAFTED = make("generic.multiversal_variant_crafted");

    public static DeferredHolder<SoundEvent, SoundEvent> make(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Palladium.id(name)));
    }

}
