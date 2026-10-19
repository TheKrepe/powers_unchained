package net.threetag.palladium.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityReference;
import net.threetag.palladium.power.ability.GrappleAbility;

public class GrappleSound extends AbstractTickableSoundLocationInstance {

    private final AbilityReference abilityReference;
    private final LivingEntity entity;
    private final float maxVolume;
    private boolean wasStopped = false;

    public GrappleSound(AbilityReference abilityReference, LivingEntity entity, Identifier soundEvent, SoundSource soundSource, float volume, float pitch) {
        super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
        this.abilityReference = abilityReference;
        this.entity = entity;
        this.volume = 0F;
        this.maxVolume = volume;
        this.pitch = pitch;
        this.looping = true;
    }

    @Override
    public void tick() {
        if (this.entity.isDeadOrDying()) {
            this.stop();
            return;
        }

        this.x = this.entity.getX();
        this.y = this.entity.getEyeY();
        this.z = this.entity.getZ();

        if (this.wasStopped) {
            if (this.volume > 0F) {
                this.volume = Mth.clamp(this.volume - 0.05F, 0F, this.maxVolume);
            }

            if (this.volume <= 0F) {
                this.stop();
            }
            return;
        }

        AbilityInstance<?> ability = this.abilityReference.getInstance(this.entity);

        // End the loop only once the grapple is fully over (the hook is gone). Tracking the hook rather than the
        // ability's enabled state keeps the loop alive for one-shot grapples, whose ability disables immediately
        // after firing. While the hook is still flying out it stays alive but silent, then fades in on latch.
        if (ability == null || !(ability.getAbility() instanceof GrappleAbility) || !GrappleAbility.hasActiveHook(this.entity, ability)) {
            this.wasStopped = true;
            return;
        }

        float target = GrappleAbility.isPulling(this.entity, ability) ? this.maxVolume : 0F;

        if (this.volume < target) {
            this.volume = Mth.clamp(this.volume + 0.05F, 0F, target);
        } else if (this.volume > target) {
            this.volume = Mth.clamp(this.volume - 0.05F, target, this.maxVolume);
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
