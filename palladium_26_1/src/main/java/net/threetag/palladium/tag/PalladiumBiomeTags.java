package net.threetag.palladium.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.threetag.palladium.Palladium;

public class PalladiumBiomeTags {

    public static final TagKey<Biome> CONTAINS_VIBRANIUM_ORE = tag("contains_vibranium_ore");
    public static final TagKey<Biome> CONTAINS_VIBRANIUM_VEINS = tag("contains_vibranium_veins");
    public static final TagKey<Biome> CONTAINS_HEART_SHAPED_HERBS = tag("contains_heart_shaped_herbs");

    private static TagKey<Biome> tag(String path) {
        return TagKey.create(Registries.BIOME, Palladium.id(path));
    }

}
