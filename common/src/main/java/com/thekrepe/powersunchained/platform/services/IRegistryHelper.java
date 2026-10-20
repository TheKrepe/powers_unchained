package com.thekrepe.powersunchained.platform.services;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.Nullable;

/**
 * Loader-specific registration. Common code declares content through this interface,
 * each loader backs it with its own registry mechanism (NeoForge: DeferredRegister).
 */
public interface IRegistryHelper {

    /** Registers a block. The id is set on the properties automatically. */
    <T extends Block> Supplier<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, UnaryOperator<BlockBehaviour.Properties> properties);

    /** Registers an item. The id is set on the properties automatically. */
    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties);

    /** Registers a sound event with a variable range, id = mod namespace + name. */
    Supplier<SoundEvent> registerSound(String name);

    /** Registers an entity type; the builder is built with the registry key. */
    <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, Supplier<EntityType.Builder<T>> builder);

    /**
     * Registers a creative tab. The loader builds it (vanilla's tab builder API is partly
     * inaccessible from common code), common code only describes it.
     *
     * @param title    tab title
     * @param icon     icon stack, created lazily
     * @param contents fills the tab, in order
     * @param after    tab that comes right before this one, or {@code null}
     */
    Supplier<CreativeModeTab> registerCreativeTab(String name, Component title, Supplier<ItemStack> icon,
            Consumer<TabOutput> contents, @Nullable Identifier after);

    /** Receives the stacks of a creative tab. */
    interface TabOutput {
        /** @param searchOnly {@code true}: only findable through the search tab, not listed in the tab itself */
        void accept(ItemStack stack, boolean searchOnly);
    }
}
