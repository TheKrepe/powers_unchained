package com.thekrepe.powersunchained.registry;

import com.thekrepe.powersunchained.platform.Services;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public final class PUCreativeTabs {

    public static final Supplier<CreativeModeTab> MAIN = Services.REGISTRY.registerCreativeTab("main", builder -> builder
            .title(Component.translatable("itemGroup.powers_unchained.main"))
            .icon(() -> PUItems.SPIDER_MAN_MASK.get().getDefaultInstance())
            .displayItems((parameters, output) -> PUItems.all().forEach(item -> output.accept(item.get())))
            );

    private PUCreativeTabs() {
    }

    public static void init() {
    }
}
