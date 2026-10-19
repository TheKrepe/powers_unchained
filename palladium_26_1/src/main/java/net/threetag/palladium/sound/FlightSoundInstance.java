package net.threetag.palladium.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.entity.flight.FlightType;

public class FlightSoundInstance extends AbstractTickableSoundLocationInstance {

    private static final float FADE_IN_STEP = 0.1F;
    private static final float FADE_OUT_STEP = 0.02F;
    private static final float STOP_FADE_STEP = 0.1F;
    private static final float MAX_VOLUME_DROP = 0.05F;

    private final LivingEntity entity;
    private final FlightType flightType;
    private final boolean ambient;
    private float fade = 0F;
    private boolean flightEnded = false;

    public FlightSoundInstance(LivingEntity entity, FlightType flightType, Identifier sound, boolean ambient) {
        super(sound, entity.getSoundSource(), SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.flightType = flightType;
        this.ambient = ambient;
        this.looping = true;
        this.volume = 0F;
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved() || this.entity.isDeadOrDying()) {
            this.stop();
            return;
        }

        this.x = this.entity.getX();
        this.y = this.entity.getEyeY();
        this.z = this.entity.getZ();

        var handler = EntityFlightHandler.get(this.entity);

        if (handler.getFlightType() != this.flightType) {
            this.flightEnded = true;
        }

        boolean propelledActive = handler.isFlightSoundActive();
        boolean active = !this.flightEnded && (this.ambient
                ? this.flightType.getFlightSound() == null || !propelledActive
                : propelledActive);
        float step = active ? FADE_IN_STEP : this.flightEnded ? -STOP_FADE_STEP : -FADE_OUT_STEP;
        this.fade = Mth.clamp(this.fade + step, 0F, 1F);

        float targetVolume;

        if (this.ambient) {
            targetVolume = this.fade;
        } else {
            float speedSqr = (float) this.entity.getDeltaMovement().lengthSqr();
            float speedVolume = speedSqr >= 1.0E-7F ? Mth.clamp(speedSqr / 4F, 0F, 1F) : 0F;
            targetVolume = this.fade * speedVolume;
        }

        this.volume = targetVolume < this.volume ? Math.max(targetVolume, this.volume - MAX_VOLUME_DROP) : targetVolume;
        this.pitch = !this.ambient && this.volume > 0.8F ? 1F + (this.volume - 0.8F) : 1F;

        if (this.flightEnded && this.fade <= 0F && this.volume <= 0F) {
            this.stop();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
