package net.threetag.palladium.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;

public class InterpolatedValue {

    private float prev;
    private float current;
    private long lastTick = Long.MIN_VALUE;

    public float get(FloatValueHolder value, DataContext context, float fallback) {
        return this.get(value.get(context, fallback));
    }

    public float get(float value) {
        var minecraft = Minecraft.getInstance();
        var tick = minecraft.level != null ? minecraft.level.getGameTime() : 0L;

        if (this.lastTick == Long.MIN_VALUE) {
            this.prev = this.current = value;
            this.lastTick = tick;
        } else if (tick != this.lastTick) {
            this.prev = this.current;
            this.current = value;
            this.lastTick = tick;
        }

        return Mth.lerp(minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false), this.prev, this.current);
    }
}
