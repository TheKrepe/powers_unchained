package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.block.SuitCreatorBlock;
import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public final class PUBlocks {

    /** Same feel as the vanilla job-site tables (smithing / fletching table): wood, axe, 2.5 strength. */
    public static final Supplier<SuitCreatorBlock> SUIT_CREATOR = Services.REGISTRY.registerBlock(
            "suit_creator",
            SuitCreatorBlock::new,
            properties -> properties
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());

    private PUBlocks() {
    }

    public static void init() {
    }
}
