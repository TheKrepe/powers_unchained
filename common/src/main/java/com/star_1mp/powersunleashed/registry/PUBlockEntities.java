package com.star_1mp.powersunleashed.registry;

import com.star_1mp.powersunleashed.block.entity.IrradiatorBlockEntity;
import com.star_1mp.powersunleashed.platform.Services;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Block entity types of the mod. */
public final class PUBlockEntities {

    public static final Supplier<BlockEntityType<IrradiatorBlockEntity>> IRRADIATOR = Services.REGISTRY.registerBlockEntity(
            "irradiator", IrradiatorBlockEntity::new, PUBlocks.IRRADIATOR);

    private PUBlockEntities() {
    }

    public static void init() {
    }
}
