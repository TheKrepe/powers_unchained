package com.star_1mp.powersunchained.platform;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.platform.services.IRegistryHelper;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    private static final DeferredRegister<net.minecraft.world.effect.MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    /** Hooks all deferred registers to the mod event bus. Call after common content has been declared. */
    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        SOUNDS.register(modEventBus);
        MENUS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, UnaryOperator<BlockBehaviour.Properties> properties) {
        return BLOCKS.registerBlock(name, factory, properties);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory, UnaryOperator<Item.Properties> properties) {
        return ITEMS.registerItem(name, factory, properties);
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.registerComponentType(name, builder);
    }

    @Override
    public Supplier<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, SoundEvent::createVariableRangeEvent);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntityType(String name, Supplier<EntityType.Builder<T>> builder) {
        return ENTITY_TYPES.register(name, id -> builder.get().build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }

    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> Supplier<net.minecraft.world.level.block.entity.BlockEntityType<T>> registerBlockEntity(
            String name, net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return BLOCK_ENTITIES.register(name, () -> new net.minecraft.world.level.block.entity.BlockEntityType<>(factory, block.get()));
    }

    @Override
    public Supplier<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> registerMobEffect(String name,
            Supplier<? extends net.minecraft.world.effect.MobEffect> effect) {
        net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.effect.MobEffect, ? extends net.minecraft.world.effect.MobEffect> holder =
                MOB_EFFECTS.register(name, effect);
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> asHolder = holder;
        return () -> asHolder;
    }

    @Override
    public <T extends net.minecraft.world.inventory.AbstractContainerMenu> Supplier<net.minecraft.world.inventory.MenuType<T>> registerMenu(
            String name, java.util.function.BiFunction<Integer, net.minecraft.world.entity.player.Inventory, T> factory) {
        return MENUS.register(name, () -> new net.minecraft.world.inventory.MenuType<T>(factory::apply, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Component title, Supplier<ItemStack> icon,
            Consumer<TabOutput> contents, @Nullable Identifier after) {
        return CREATIVE_TABS.register(name, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder()
                    .title(title)
                    .icon(icon)
                    .displayItems((parameters, output) -> contents.accept((stack, searchOnly) -> output.accept(stack,
                            searchOnly ? CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY : CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));
            if (after != null) {
                builder.withTabsBefore(after);
            }
            return builder.build();
        });
    }
}
