package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.block.TailoringTableBlock;
import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public final class PUBlocks {

    public static final Supplier<TailoringTableBlock> TAILORING_TABLE = Services.REGISTRY.registerBlock(
            "tailoring_table",
            TailoringTableBlock::new,
            properties -> properties
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    private PUBlocks() {
    }

    public static void init() {
    }
}
