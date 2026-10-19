package net.threetag.palladium.datagen.internal;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.sound.PalladiumSoundEvents;

public class PalladiumSoundProvider extends SoundDefinitionsProvider {

    public PalladiumSoundProvider(PackOutput output) {
        super(output, Palladium.MOD_ID);
    }

    @Override
    public void registerSounds() {
        this.addSimple(PalladiumSoundEvents.PLAYER_FLIGHT_WEB_SHOOT, 3);
        this.addEventReference(PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED, Identifier.withDefaultNamespace("item.elytra.flying"));
        this.addEventReference(PalladiumSoundEvents.PLAYER_FLIGHT_SWING, Identifier.withDefaultNamespace("item.elytra.flying"));
        this.addSimple(PalladiumSoundEvents.MULTIVERSE_SEARCH);
        this.addSimple(PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED);
    }

    private void addEventReference(DeferredHolder<SoundEvent, SoundEvent> soundEvent, Identifier referencedEvent) {
        this.add(soundEvent, definition()
                .with(sound(referencedEvent, SoundDefinition.SoundType.EVENT))
                .subtitle(subtitle(soundEvent)));
    }

    private void addSimple(DeferredHolder<SoundEvent, SoundEvent> soundEvent) {
        this.add(soundEvent, definition()
                .with(sound(Identifier.fromNamespaceAndPath(soundEvent.getId().getNamespace(), soundEvent.getId().getPath().replace('.', '/'))))
                .subtitle(subtitle(soundEvent)));
    }

    private void addSimple(DeferredHolder<SoundEvent, SoundEvent> soundEvent, int variants) {
        var definition = definition().subtitle(subtitle(soundEvent));

        for (int i = 1; i <= variants; i++) {
            definition.with(sound(Identifier.fromNamespaceAndPath(soundEvent.getId().getNamespace(), soundEvent.getId().getPath().replace('.', '/')).withSuffix("_" + i)));
        }

        this.add(soundEvent, definition);
    }

    public static String subtitle(DeferredHolder<SoundEvent, SoundEvent> supplier) {
        return "subtitles." + Palladium.MOD_ID + "." + supplier.getId().getPath();
    }
}
