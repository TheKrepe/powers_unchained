package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public final class PUCreativeTabs {

    public static final Supplier<CreativeModeTab> MAIN = Services.REGISTRY.registerCreativeTab(
            "main",
            Component.translatable("itemGroup.powers_unchained.main"),
            () -> PUItems.SPIDER_MAN_MASK.get().getDefaultInstance(),
            PUItems::all);

    private PUCreativeTabs() {
    }

    public static void init() {
    }
}
