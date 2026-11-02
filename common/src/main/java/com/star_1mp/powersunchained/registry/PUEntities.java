package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.entity.web.ImpactWeb;
import com.star_1mp.powersunchained.entity.web.StickyWeb;
import com.star_1mp.powersunchained.entity.web.WebCocoon;
import com.star_1mp.powersunchained.entity.web.WebSwingLine;
import com.star_1mp.powersunchained.entity.web.WebZipLine;
import com.star_1mp.powersunchained.platform.Services;
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

    public static final Supplier<EntityType<com.star_1mp.powersunchained.entity.web.ElectroWeb>> ELECTRO_WEB = Services.REGISTRY.registerEntityType("electro_web",
            () -> EntityType.Builder.<com.star_1mp.powersunchained.entity.web.ElectroWeb>of(com.star_1mp.powersunchained.entity.web.ElectroWeb::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1));

    public static final Supplier<EntityType<WebCocoon>> WEB_COCOON = Services.REGISTRY.registerEntityType("web_cocoon",
            () -> EntityType.Builder.<WebCocoon>of(WebCocoon::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1));
    public static final Supplier<EntityType<WebZipLine>> WEB_ZIP_LINE = Services.REGISTRY.registerEntityType("web_zip_line",
            () -> EntityType.Builder.<WebZipLine>of(WebZipLine::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1));

    public static final Supplier<EntityType<WebSwingLine>> WEB_SWING_LINE = Services.REGISTRY.registerEntityType("web_swing_line",
            () -> EntityType.Builder.<WebSwingLine>of(WebSwingLine::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.3F, 0.3F).clientTrackingRange(10).updateInterval(1));

    /** Cave spider size (owner); the cave spider loot table is copied 1:1 into its own. */
    public static final Supplier<EntityType<com.star_1mp.powersunchained.entity.spider.RadioactiveSpider>> RADIOACTIVE_SPIDER =
            Services.REGISTRY.registerEntityType("radioactive_spider",
                    () -> EntityType.Builder.<com.star_1mp.powersunchained.entity.spider.RadioactiveSpider>of(
                                    com.star_1mp.powersunchained.entity.spider.RadioactiveSpider::new, MobCategory.MONSTER)
                            .sized(0.7F, 0.5F).eyeHeight(0.45F).passengerAttachments(0.5F).clientTrackingRange(8));

    private PUEntities() {
    }

    public static void init() {
    }
}
