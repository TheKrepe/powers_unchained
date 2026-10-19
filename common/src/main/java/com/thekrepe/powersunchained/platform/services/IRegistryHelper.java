package com.thekrepe.powersunchained.platform.services;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Loader-specific registration. Common code declares content through this interface,
 * each loader backs it with its own registry mechanism (NeoForge: DeferredRegister).
 */
public interface IRegistryHelper {

    /** Registers an item. The id is set on the properties automatically. */
    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties);

    /**
     * Registers a creative tab. The loader builds it (vanilla's tab builder API is partly
     * inaccessible from common code), common code only describes it.
     *
     * @param title tab title
     * @param icon  icon stack, created lazily
     * @param items items shown in the tab, in order
     */
    Supplier<CreativeModeTab> registerCreativeTab(String name, Component title, Supplier<ItemStack> icon,
            Supplier<? extends Collection<? extends Supplier<? extends Item>>> items);
}
