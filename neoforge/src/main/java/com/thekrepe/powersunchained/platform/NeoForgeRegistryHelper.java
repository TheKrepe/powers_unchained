package com.thekrepe.powersunchained.platform;

import com.thekrepe.powersunchained.Constants;
import com.thekrepe.powersunchained.platform.services.IRegistryHelper;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    /** Hooks all deferred registers to the mod event bus. Call after common content has been declared. */
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties) {
        return ITEMS.registerItem(name, factory, properties);
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Component title, Supplier<ItemStack> icon,
            Supplier<? extends Collection<? extends Supplier<? extends Item>>> items) {
        return CREATIVE_TABS.register(name, () -> CreativeModeTab.builder()
                .title(title)
                .icon(icon)
                .displayItems((parameters, output) -> items.get().forEach(item -> output.accept(item.get())))
                .build());
    }
}
