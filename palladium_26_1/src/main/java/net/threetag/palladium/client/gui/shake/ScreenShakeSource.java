package net.threetag.palladium.client.gui.shake;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;

public interface ScreenShakeSource {

    ScreenShake.ShakeIntensity intensity();

    float frequency();

    float strength(float partialTick);

    static ScreenShakeSource of(ScreenShake.ShakeIntensity intensity, float frequency, Float2FloatFunction strength) {
        return new Simple(intensity, frequency, strength);
    }

    record Simple(ScreenShake.ShakeIntensity intensity, float frequency,
                  Float2FloatFunction strengthFunction) implements ScreenShakeSource {

        @Override
        public float strength(float partialTick) {
            return this.strengthFunction.get(partialTick);
        }
    }
}
