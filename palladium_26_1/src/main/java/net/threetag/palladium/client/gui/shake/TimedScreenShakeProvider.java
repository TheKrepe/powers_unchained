package net.threetag.palladium.client.gui.shake;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TimedScreenShakeProvider implements ScreenShakeProvider {

    private final Function<LocalPlayer, @Nullable ScreenShake> shakeGetter;
    @Nullable
    private State state;

    public TimedScreenShakeProvider(Function<LocalPlayer, @Nullable ScreenShake> shakeGetter) {
        this.shakeGetter = shakeGetter;
    }

    @Override
    public void provide(LocalPlayer player, BiConsumer<Object, ScreenShakeSource> consumer) {
        ScreenShake shake = this.shakeGetter.apply(player);

        if (shake != null) {
            if (this.state == null || this.state.isDone() || !shake.equals(this.state.shake)) {
                this.state = new State(shake);
            }
            this.state.tick(true);
        } else if (this.state != null) {
            if (this.state.isDone()) {
                this.state = null;
                return;
            }
            this.state.tick(false);
        }

        if (this.state != null) {
            consumer.accept(this, this.state);
        }
    }

    @Override
    public void reset() {
        this.state = null;
    }

    private static class State implements ScreenShakeSource {

        final ScreenShake shake;
        int fadeInTick = 0;
        int fadeOutTick = -1;

        State(ScreenShake shake) {
            this.shake = shake;
        }

        void tick(boolean active) {
            if (active) {
                this.fadeOutTick = -1;
                if (this.fadeInTick < this.shake.fadeInDuration()) this.fadeInTick++;
            } else {
                if (this.fadeOutTick < 0) this.fadeOutTick = 0;
                else this.fadeOutTick++;
            }
        }

        boolean isDone() {
            if (this.fadeOutTick < 0) return false;
            int fadeOut = this.shake.fadeOutDuration();
            return fadeOut == 0 || this.fadeOutTick >= fadeOut;
        }

        @Override
        public ScreenShake.ShakeIntensity intensity() {
            return this.shake.intensity();
        }

        @Override
        public float frequency() {
            return this.shake.frequency();
        }

        @Override
        public float strength(float partialTick) {
            float mult = 1.0f;
            int fadeIn = this.shake.fadeInDuration();
            if (fadeIn > 0 && this.fadeInTick < fadeIn) {
                mult *= this.shake.easing().in().apply((float) this.fadeInTick / fadeIn);
            }
            if (this.fadeOutTick >= 0) {
                int fadeOut = this.shake.fadeOutDuration();
                if (fadeOut <= 0) return 0;
                mult *= 1.0f - this.shake.easing().out().apply((float) this.fadeOutTick / fadeOut);
            }
            return mult;
        }
    }
}
