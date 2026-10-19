package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.block.SuitCreatorBlock;
import com.thekrepe.powersunchained.platform.Services;
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

    private PUBlocks() {
    }

    public static void init() {
    }
}
