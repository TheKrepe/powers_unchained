package net.threetag.palladium.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterTooltipAppendersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.Openable;
import net.threetag.palladium.item.multiverse.ItemMultiversalVariant;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.item.upgrade.ItemUpgrades;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.BlastAbility;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class PalladiumDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Palladium.MOD_ID);

    static {
        Items.RENDER_LAYERS.getId();
        Abilities.UNLOCKED.getId();
    }

    @SubscribeEvent
    static void addTooltips(RegisterTooltipAppendersEvent e) {
        e.registerComponentAppenderBeforeAll(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get(), (stack, context, display, player, tooltipFlag, builder) -> {
            stack.addToTooltip(PalladiumDataComponents.Items.MULTIVERSAL_VARIANT.get(), context, display, builder, tooltipFlag);
        });
        e.registerComponentAppenderBefore(PalladiumDataComponents.Items.ITEM_UPGRADES.get(), DataComponents.ATTRIBUTE_MODIFIERS, (stack, context, display, player, tooltipFlag, builder) -> {
            stack.addToTooltip(PalladiumDataComponents.Items.ITEM_UPGRADES.get(), context, display, builder, tooltipFlag);
        });
    }

    public static class Items {

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<SlotDependentIdComponent>> RENDER_LAYERS = DATA_COMPONENTS.registerComponentType("render_layers", builder -> builder
                .persistent(SlotDependentIdComponent.CODEC)
                .networkSynchronized(SlotDependentIdComponent.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemPowers>> POWERS = DATA_COMPONENTS.registerComponentType("powers", builder -> builder
                .persistent(ItemPowers.CODEC)
                .networkSynchronized(ItemPowers.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<SlotDependentHolderSetComponent<Power>>> POWER_DAMPENING = DATA_COMPONENTS.registerComponentType("power_dampening", builder -> builder
                .persistent(SlotDependentHolderSetComponent.codec(PalladiumRegistryKeys.POWER)));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> BOTTLE_ENTITY_DATA = DATA_COMPONENTS.registerComponentType("bottle_entity_data", builder -> builder
                .persistent(CustomData.CODEC)
                .networkSynchronized(CustomData.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Openable>> OPENABLE = DATA_COMPONENTS.registerComponentType("openable", builder -> builder
                .persistent(Openable.CODEC)
                .networkSynchronized(Openable.STREAM_CODEC));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> OPENED = DATA_COMPONENTS.registerComponentType("opened", builder -> builder
                .ignoreSwapAnimation()
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OPEN_TIMER = DATA_COMPONENTS.registerComponentType("open_timer", builder -> builder
                .ignoreSwapAnimation()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> MULTIVERSAL_CATEGORY = DATA_COMPONENTS.registerComponentType("multiversal_category", builder -> builder
                .ignoreSwapAnimation()
                .persistent(Identifier.CODEC)
                .networkSynchronized(Identifier.STREAM_CODEC));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemMultiversalVariant>> MULTIVERSAL_VARIANT = DATA_COMPONENTS.registerComponentType("multiversal_variant", builder -> builder
                .ignoreSwapAnimation()
                .persistent(ItemMultiversalVariant.CODEC)
                .networkSynchronized(ItemMultiversalVariant.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<Universe>>> EXTRAPOLATOR_UNIVERSE = DATA_COMPONENTS.registerComponentType("extrapolator_universe", builder -> builder
                .persistent(ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE))
                .networkSynchronized(ResourceKey.streamCodec(PalladiumRegistryKeys.MULTIVERSE)));
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EXTRAPOLATOR_SEARCH_TIMER = DATA_COMPONENTS.registerComponentType("extrapolator_search_timer", builder -> builder
                .ignoreSwapAnimation()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemUpgrades>> ITEM_UPGRADES = DATA_COMPONENTS.registerComponentType("upgrades", builder -> builder
                .persistent(ItemUpgrades.CODEC)
                .networkSynchronized(ItemUpgrades.STREAM_CODEC));
    }

    public static class Abilities {

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> UNLOCKED = DATA_COMPONENTS.registerComponentType("unlocked", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ENABLED = DATA_COMPONENTS.registerComponentType("enabled", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> KEY_PRESSED = DATA_COMPONENTS.registerComponentType("key_pressed", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COOLDOWN = DATA_COMPONENTS.registerComponentType("cooldown", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COOLDOWN_MAX = DATA_COMPONENTS.registerComponentType("cooldown_max", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ACTIVATION_TIME = DATA_COMPONENTS.registerComponentType("activation_time", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ACTIVATION_TIME_MAX = DATA_COMPONENTS.registerComponentType("activation_time_max", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BOUGHT = DATA_COMPONENTS.registerComponentType("bought", builder -> builder
                .persistent(Codec.BOOL)
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Vec3>> ENERGY_BEAM_TARGET = DATA_COMPONENTS.registerComponentType("energy_beam_target", builder -> builder
                .networkSynchronized(ByteBufCodecs.VECTOR3F.map(Vec3::new, Vec3::toVector3f)));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlastAbility.ActiveBlast>> BLAST = DATA_COMPONENTS.registerComponentType("blast", builder -> builder
                .networkSynchronized(BlastAbility.ActiveBlast.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Component>> NAME_CHANGE_CACHED = DATA_COMPONENTS.registerComponentType("name_change_cached", builder -> builder
                .networkSynchronized(ComponentSerialization.STREAM_CODEC));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> GUARD_DAMAGE_ABSORBED = DATA_COMPONENTS.registerComponentType("guard_damage_absorbed", builder -> builder
                .persistent(Codec.FLOAT)
                .networkSynchronized(ByteBufCodecs.FLOAT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> GUARD_JUST_BROKE = DATA_COMPONENTS.registerComponentType("guard_just_broke", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GUARD_BREAK_LOCKOUT = DATA_COMPONENTS.registerComponentType("guard_break_lockout", builder -> builder
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MULTI_JUMPS_REMAINING = DATA_COMPONENTS.registerComponentType("multi_jumps_remaining", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WAS_JUMPING = DATA_COMPONENTS.registerComponentType("was_jumping", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> MULTI_JUMP_ARMED = DATA_COMPONENTS.registerComponentType("multi_jump_armed", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MULTI_JUMP_TIMER = DATA_COMPONENTS.registerComponentType("multi_jump_timer", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));
        

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SLIDE_TICKS_REMAINING = DATA_COMPONENTS.registerComponentType("slide_ticks_remaining", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIDE_WAS_SHIFT_DOWN = DATA_COMPONENTS.registerComponentType("slide_was_shift_down", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIDE_WAS_SPRINTING = DATA_COMPONENTS.registerComponentType("slide_was_sprinting", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> SLIDE_VELOCITY_X = DATA_COMPONENTS.registerComponentType("slide_velocity_x", builder -> builder
                .networkSynchronized(ByteBufCodecs.DOUBLE));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> SLIDE_VELOCITY_Z = DATA_COMPONENTS.registerComponentType("slide_velocity_z", builder -> builder
                .networkSynchronized(ByteBufCodecs.DOUBLE));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIDE_LOCKED = DATA_COMPONENTS.registerComponentType("slide_locked", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIDE_WAS_MOVEMENT_INPUT = DATA_COMPONENTS.registerComponentType("slide_was_movement_input", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIDE_ACTIVE = DATA_COMPONENTS.registerComponentType("slide_active", builder -> builder
                .networkSynchronized(ByteBufCodecs.BOOL));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GRAPPLE_HOOK_ID = DATA_COMPONENTS.registerComponentType("grapple_hook_id", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MODE_SWITCH_ACTIVE_KEY = DATA_COMPONENTS.registerComponentType("mode_switch_active_key", builder -> builder
                .networkSynchronized(ByteBufCodecs.STRING_UTF8));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMBO_ATTACK_COUNT = DATA_COMPONENTS.registerComponentType("combo_attack_count", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMBO_ATTACK_RESET_TIMER = DATA_COMPONENTS.registerComponentType("combo_attack_reset_timer", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMBO_ATTACK_PENDING_TARGET = DATA_COMPONENTS.registerComponentType("combo_attack_pending_target", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COMBO_ATTACK_FINAL_TIMER = DATA_COMPONENTS.registerComponentType("combo_attack_final_timer", builder -> builder
                .networkSynchronized(ByteBufCodecs.VAR_INT));

        private static DataComponentMap COMMON_COMPONENTS = null;

        public static DataComponentMap getCommonComponents() {
            if (COMMON_COMPONENTS == null) {
                COMMON_COMPONENTS = DataComponentMap.builder()
                        .set(UNLOCKED.get(), false)
                        .set(ENABLED.get(), false)
                        .build();
            }

            return COMMON_COMPONENTS;
        }
    }
}
