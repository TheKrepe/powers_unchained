package com.star_1mp.powersunleashed.registry;

import com.star_1mp.powersunleashed.irradiation.RadioactiveFeverEffect;
import com.star_1mp.powersunleashed.platform.Services;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/** Mob effects of the mod. */
public final class PUEffects {

    public static final Supplier<Holder<MobEffect>> RADIOACTIVE_FEVER = Services.REGISTRY.registerMobEffect("radioactive_fever",
            RadioactiveFeverEffect::new);

    private PUEffects() {
    }

    public static void init() {
    }
}
