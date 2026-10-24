package com.star_1mp.powersunchained.client.web;

import com.star_1mp.powersunchained.registry.PUSounds;
import java.util.function.Predicate;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Looping web swinging sounds that follow a player, louder the faster they go (like Fisk's sound scripts):
 * {@link #wind wind} and {@link #stress rope stress} while hanging on a rope, {@link #fall wind} while falling after
 * letting go.
 */
public final class WebSwingSound extends AbstractTickableSoundInstance {

    private final Entity entity;
    private final Predicate<Entity> keepPlaying;
    private final float maxVolume;
    private final boolean fallPitch;
    private int ticks;
    private int fadeOut = -1;
    private static final int FADE_OUT = 10;

    private WebSwingSound(SoundEvent sound, Entity entity, float maxVolume, boolean fallPitch, Predicate<Entity> keepPlaying) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.keepPlaying = keepPlaying;
        this.maxVolume = maxVolume;
        this.fallPitch = fallPitch;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0001F;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    /** Wind while swinging. */
    public static WebSwingSound wind(Entity entity, Predicate<Entity> swinging) {
        return new WebSwingSound(PUSounds.WEB_SWING_WIND.get(), entity, 0.4F, false, swinging);
    }

    /** Rope stress while swinging. */
    public static WebSwingSound stress(Entity entity, Predicate<Entity> swinging) {
        return new WebSwingSound(PUSounds.WEB_SWING_STRESS.get(), entity, 0.1F, false, swinging);
    }

    /** Wind while falling / diving after letting go. */
    public static WebSwingSound fall(Entity entity, Predicate<Entity> falling) {
        return new WebSwingSound(PUSounds.WEB_SWING_WIND.get(), entity, 0.5F, true, falling);
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved()) {
            this.stop();
            return;
        }
        this.ticks++;
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
        if (this.fadeOut < 0 && this.ticks > 2 && !this.keepPlaying.test(this.entity)) {
            this.fadeOut = FADE_OUT;
        }
        Vec3 motion = this.entity.position().subtract(this.entity.xo, this.entity.yo, this.entity.zo);
        float vel = (float) motion.length();
        float v = vel >= 0.01F ? Mth.clamp(vel * vel / 4.0F, 0.0F, 1.0F) : 0.0F;
        float fade = Math.min(this.ticks / 10.0F, 1.0F);
        if (this.fadeOut >= 0) {
            fade *= this.fadeOut / (float) FADE_OUT;
            if (--this.fadeOut < 0) {
                this.stop();
                return;
            }
        }
        if (this.fallPitch) {
            this.pitch = 0.5F + 0.2F * fade + 0.5F * Math.min(vel, 1.0F);
        } else {
            this.pitch = v > 0.7F ? 0.3F + v : 1.0F;
        }
        this.volume = Math.max(v * fade * this.maxVolume, 0.0001F);
    }
}
