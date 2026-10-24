package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public final class PUSounds {

    public static final Supplier<SoundEvent> WEB_SHOOT = Services.REGISTRY.registerSound("web_shooters.shoot");
    public static final Supplier<SoundEvent> WEB_SWITCH = Services.REGISTRY.registerSound("web_shooters.switch");
    public static final Supplier<SoundEvent> WEB_SWING_ENABLE = Services.REGISTRY.registerSound("web_swinging.enable");
    public static final Supplier<SoundEvent> WEB_SWING_DISABLE = Services.REGISTRY.registerSound("web_swinging.disable");
    public static final Supplier<SoundEvent> WEB_SWING_WHOOSH = Services.REGISTRY.registerSound("web_swinging.whoosh");
    public static final Supplier<SoundEvent> WEB_SWING_STRESS = Services.REGISTRY.registerSound("web_swinging.stress");
    public static final Supplier<SoundEvent> WEB_SWING_WIND = Services.REGISTRY.registerSound("web_swinging.wind");
    public static final Supplier<SoundEvent> WEB_BOUNCE = Services.REGISTRY.registerSound("entity.web.bounce");

    private PUSounds() {
    }

    public static void init() {
    }
}
