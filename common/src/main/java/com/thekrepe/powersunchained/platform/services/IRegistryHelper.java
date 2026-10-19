package com.thekrepe.powersunchained.platform.services;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * Loader-specific registration. Common code declares content through this interface,
 * each loader backs it with its own registry mechanism (NeoForge: DeferredRegister).
 */
public interface IRegistryHelper {

    /** Registers an item. The id is set on the properties automatically. */
    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties);

    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab);
}
