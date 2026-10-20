package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.entity.WebProjectile;
import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class PUEntities {

    public static final Supplier<EntityType<WebProjectile>> IMPACT_WEB = web("impact_web");
    public static final Supplier<EntityType<WebProjectile>> STICKY_WEB = web("sticky_web");

    private PUEntities() {
    }

    public static void init() {
    }

    private static Supplier<EntityType<WebProjectile>> web(String name) {
        return Services.REGISTRY.registerEntityType(name, () -> EntityType.Builder.<WebProjectile>of(WebProjectile::new, MobCategory.MISC)
                .noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10));
    }
}
