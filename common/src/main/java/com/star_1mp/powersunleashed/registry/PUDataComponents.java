package com.star_1mp.powersunleashed.registry;

import com.mojang.serialization.Codec;
import com.star_1mp.powersunleashed.platform.Services;
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

    /** Upgrade modules installed in web-shooters (Tech Table), in order of the module slots. */
    public static final Supplier<DataComponentType<List<String>>> WEB_MODULES = Services.REGISTRY.registerDataComponent("web_modules",
            builder -> builder.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    /** Modules unlocked ON THIS pair of web-shooters (owner: unlocking is per device; installing is free). */
    public static final Supplier<DataComponentType<List<String>>> UNLOCKED_MODULES = Services.REGISTRY.registerDataComponent("unlocked_web_modules",
            builder -> builder.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    /** Looks unlocked ON THIS gadget (owner: bought per pair of web-shooters, like modules). */
    public static final Supplier<DataComponentType<List<String>>> UNLOCKED_SKINS = Services.REGISTRY.registerDataComponent("unlocked_gadget_skins",
            builder -> builder.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    /** Look (design) of a gadget (Tech Table, Customization tab); missing = default. */
    public static final Supplier<DataComponentType<String>> GADGET_SKIN = Services.REGISTRY.registerDataComponent("gadget_skin",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private PUDataComponents() {
    }

    public static void init() {
    }
}
