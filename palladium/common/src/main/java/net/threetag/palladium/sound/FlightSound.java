package net.threetag.palladium.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.entity.FlightHandler;
import net.threetag.palladium.entity.PalladiumPlayerExtension;

public class FlightSound extends AbstractTickableSoundInstance {

    private static final float FADE_IN_STEP = 0.1F;
    private static final float FADE_OUT_STEP = 0.02F;
    private static final float STOP_FADE_STEP = 0.1F;
    private static final float MAX_VOLUME_DROP = 0.05F;

    public final LivingEntity entity;
    private float fade = 0F;
    public boolean stop;

    public FlightSound(LivingEntity entity, SoundEvent soundEvent, SoundSource category) {
        super(soundEvent, category, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 0F;
    }

    @Override
    public void tick() {
        if (!this.entity.isAlive() || !(this.entity instanceof PalladiumPlayerExtension extension)) {
            this.stop();
            FlightHandler.CACHED_SOUND = null;
            return;
        }

        var flight = extension.palladium$getFlightHandler();
        boolean active = !this.stop && flight.isPropelling() && flight.getFlightType().isNotNull();
        float step = active ? FADE_IN_STEP : this.stop ? -STOP_FADE_STEP : -FADE_OUT_STEP;
        this.fade = Mth.clamp(this.fade + step, 0F, 1F);

        if (this.fade <= 0F && !active) {
            this.stop();

            if (FlightHandler.CACHED_SOUND == this) {
                FlightHandler.CACHED_SOUND = null;
            }

            return;
        }

        this.x = (float) this.entity.getX();
        this.y = (float) this.entity.getY();
        this.z = (float) this.entity.getZ();

        float speedSqr = (float) this.entity.getDeltaMovement().lengthSqr();
        float speedVolume = speedSqr >= 1.0E-7F ? Mth.clamp(speedSqr / 4F, 0F, 1F) : 0F;
        float targetVolume = this.fade * speedVolume;

        this.volume = targetVolume < this.volume ? Math.max(targetVolume, this.volume - MAX_VOLUME_DROP) : targetVolume;
        this.pitch = this.volume > 0.8F ? 1F + (this.volume - 0.8F) : 1F;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
