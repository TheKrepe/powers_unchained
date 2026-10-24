package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.block.SuitCreatorBlock;
import com.star_1mp.powersunchained.block.TechTableBlock;
import com.star_1mp.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public final class PUBlocks {

    /** Steel machine: pickaxe, strength 3.5 like other metal workstations, metal sound. */
    public static final Supplier<SuitCreatorBlock> SUIT_CREATOR = Services.REGISTRY.registerBlock(
            "suit_creator",
            SuitCreatorBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    /** Wooden workbench with steel parts: axe, strength 2.5 like the crafting table, wood sound. */
    public static final Supplier<TechTableBlock> TECH_TABLE = Services.REGISTRY.registerBlock(
            "tech_table",
            TechTableBlock::new,
            properties -> properties
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    private PUBlocks() {
    }

    public static void init() {
    }
}
