package com.star_1mp.powersunleashed.registry;

import com.star_1mp.powersunleashed.block.SuitCreatorBlock;
import com.star_1mp.powersunleashed.block.TechTableBlock;
import com.star_1mp.powersunleashed.platform.Services;
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

    /** Steel + lead machine with a glowing core: pickaxe, like the Suit Creator; light 7 idle, 13 while irradiating. */
    public static final Supplier<com.star_1mp.powersunleashed.block.IrradiatorBlock> IRRADIATOR = Services.REGISTRY.registerBlock(
            "irradiator",
            com.star_1mp.powersunleashed.block.IrradiatorBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(com.star_1mp.powersunleashed.block.IrradiatorBlock.ACTIVE) ? 13 : 7)
                    .noOcclusion());

    private PUBlocks() {
    }

    public static void init() {
    }
}
