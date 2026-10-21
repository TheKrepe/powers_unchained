package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.entity.web.ImpactWeb;
import com.thekrepe.powersunchained.entity.web.StickyWeb;
import com.thekrepe.powersunchained.entity.web.WebCocoon;
import com.thekrepe.powersunchained.entity.web.WebZipLine;
import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class PUEntities {

    public static final Supplier<EntityType<ImpactWeb>> IMPACT_WEB = Services.REGISTRY.registerEntityType("impact_web",
            () -> EntityType.Builder.<ImpactWeb>of(ImpactWeb::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1));
    public static final Supplier<EntityType<StickyWeb>> STICKY_WEB = Services.REGISTRY.registerEntityType("sticky_web",
            () -> EntityType.Builder.<StickyWeb>of(StickyWeb::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1));

    public static final Supplier<EntityType<WebCocoon>> WEB_COCOON = Services.REGISTRY.registerEntityType("web_cocoon",
            () -> EntityType.Builder.<WebCocoon>of(WebCocoon::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1));
    public static final Supplier<EntityType<WebZipLine>> WEB_ZIP_LINE = Services.REGISTRY.registerEntityType("web_zip_line",
            () -> EntityType.Builder.<WebZipLine>of(WebZipLine::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1));

    private PUEntities() {
    }

    public static void init() {
    }
}
