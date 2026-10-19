package net.threetag.palladium.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.threetag.palladium.Palladium;

public class PalladiumPlacedFeatureTags {

    public static final TagKey<PlacedFeature> LEAD_ORES = tag("lead_ores");
    public static final TagKey<PlacedFeature> TITANIUM_ORES = tag("titanium_ores");
    public static final TagKey<PlacedFeature> VIBRANIUM_ORES = tag("vibranium_ores");
    public static final TagKey<PlacedFeature> VIBRANIUM_VEINS = tag("vibranium_veins");
    public static final TagKey<PlacedFeature> HEART_SHAPED_HERBS = tag("heart_shaped_herbs");

    private static TagKey<PlacedFeature> tag(String path) {
        return TagKey.create(Registries.PLACED_FEATURE, Palladium.id(path));
    }

}
