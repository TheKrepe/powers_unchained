package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public final class PUSounds {

    public static final Supplier<SoundEvent> WEB_SHOOT = Services.REGISTRY.registerSound("web_shooters.shoot");
    public static final Supplier<SoundEvent> WEB_SWITCH = Services.REGISTRY.registerSound("web_shooters.switch");
    public static final Supplier<SoundEvent> WEB_BOUNCE = Services.REGISTRY.registerSound("entity.web.bounce");

    private PUSounds() {
    }

    public static void init() {
    }
}
