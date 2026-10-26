package com.star_1mp.powersunchained.registry;

import com.mojang.serialization.Codec;
import com.star_1mp.powersunchained.platform.Services;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

/** Item data components of the mod. */
public final class PUDataComponents {

    /** Web fluid left in a web cartridge (missing = full). */
    public static final Supplier<DataComponentType<Integer>> WEB_FLUID = Services.REGISTRY.registerDataComponent("web_fluid",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Cartridges loaded into web-shooters: the web fluid of each (0 = used-up cartridge). */
    public static final Supplier<DataComponentType<List<Integer>>> WEB_CARTRIDGES = Services.REGISTRY.registerDataComponent("web_cartridges",
            builder -> builder.persistent(Codec.INT.listOf()).networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())));

    /** Creative web-shooters: endless web fluid (same item as the normal web-shooters). */
    public static final Supplier<DataComponentType<Boolean>> CREATIVE_WEB_SHOOTERS = Services.REGISTRY.registerDataComponent("creative_web_shooters",
            builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private PUDataComponents() {
    }

    public static void init() {
    }
}
