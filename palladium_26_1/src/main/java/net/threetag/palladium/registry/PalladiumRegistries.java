package net.threetag.palladium.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.CustomizationCategory;
import net.threetag.palladium.customization.CustomizationSerializer;
import net.threetag.palladium.entity.data.PalladiumEntityDataType;
import net.threetag.palladium.entity.effect.EntityEffect;
import net.threetag.palladium.entity.flight.FlightType;
import net.threetag.palladium.entity.flight.FlightTypeSerializer;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.icon.IconSerializer;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.item.upgrade.ItemUpgrade;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.cost.CostSerializer;
import net.threetag.palladium.logic.value.ValueSerializer;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.enabling.EnablingHandler;
import net.threetag.palladium.power.ability.keybind.KeyBindTypeSerializer;
import net.threetag.palladium.power.ability.unlocking.UnlockingHandler;
import net.threetag.palladium.power.dampening.PowerDampeningSource;
import net.threetag.palladium.power.provider.PowerProvider;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class PalladiumRegistries {

    public static final Registry<PalladiumEntityDataType<?>> ENTITY_DATA_TYPE = new RegistryBuilder<>(PalladiumRegistryKeys.ENTITY_DATA_TYPE).create();
    public static final Registry<AbilitySerializer<?>> ABILITY_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.ABILITY_SERIALIZER).create();
    public static final Registry<MapCodec<? extends UnlockingHandler>> ABILITY_UNLOCKING_HANDLER = new RegistryBuilder<>(PalladiumRegistryKeys.ABILITY_UNLOCKING_HANDLER).create();
    public static final Registry<MapCodec<? extends EnablingHandler>> ABILITY_ENABLING_HANDLER = new RegistryBuilder<>(PalladiumRegistryKeys.ABILITY_ENABLING_HANDLER).create();
    public static final Registry<KeyBindTypeSerializer<?>> KEY_BIND_TYPE_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.KEY_BIND_TYPE_SERIALIZER).create();
    public static final Registry<PowerProvider> POWER_PROVIDER = new RegistryBuilder<>(PalladiumRegistryKeys.POWER_PROVIDER).create();
    public static final Registry<PowerDampeningSource> POWER_DAMPENING_SOURCE = new RegistryBuilder<>(PalladiumRegistryKeys.POWER_DAMPENING_SOURCE).create();
    public static final Registry<ActionSerializer<?>> ACTION_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.ACTION_SERIALIZER).create();
    public static final Registry<ConditionSerializer<?>> CONDITION_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.CONDITION_SERIALIZER).create();
    public static final Registry<ValueSerializer<?>> VALUE_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.VALUE_SERIALIZER).create();
    public static final Registry<CostSerializer<?>> COST_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.COST_SERIALIZER).create();
    public static final Registry<IconSerializer<?>> ICON_SERIALIZER = new RegistryBuilder<>(PalladiumRegistryKeys.ICON_SERIALIZER).create();
    public static final Registry<MapCodec<? extends Item>> ITEM_TYPE = new RegistryBuilder<>(PalladiumRegistryKeys.ITEM_TYPE).create();
    public static final Registry<EntityEffect> ENTITY_EFFECT = new RegistryBuilder<>(PalladiumRegistryKeys.ENTITY_EFFECT).create();
    public static final Registry<CustomizationSerializer<?>> CUSTOMIZATION_SERIALIZERS = new RegistryBuilder<>(PalladiumRegistryKeys.CUSTOMIZATION_SERIALIZERS).create();
    public static final Registry<FlightTypeSerializer<?>> FLIGHT_TYPE_SERIALIZERS = new RegistryBuilder<>(PalladiumRegistryKeys.FLIGHT_TYPE_SERIALIZERS).create();

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent e) {
        e.register(ENTITY_DATA_TYPE);
        e.register(ABILITY_SERIALIZER);
        e.register(ABILITY_UNLOCKING_HANDLER);
        e.register(ABILITY_ENABLING_HANDLER);
        e.register(KEY_BIND_TYPE_SERIALIZER);
        e.register(POWER_PROVIDER);
        e.register(POWER_DAMPENING_SOURCE);
        e.register(ACTION_SERIALIZER);
        e.register(CONDITION_SERIALIZER);
        e.register(VALUE_SERIALIZER);
        e.register(COST_SERIALIZER);
        e.register(ICON_SERIALIZER);
        e.register(ITEM_TYPE);
        e.register(ENTITY_EFFECT);
        e.register(CUSTOMIZATION_SERIALIZERS);
        e.register(FLIGHT_TYPE_SERIALIZERS);
    }

    @SubscribeEvent
    static void dataPackRegistries(DataPackRegistryEvent.NewRegistry e) {
        e.dataPackRegistry(PalladiumRegistryKeys.POWER, Power.CODEC, Power.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY, CustomizationCategory.CODEC, CustomizationCategory.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.CUSTOMIZATION, Customization.Codecs.SIMPLE_CODEC, Customization.Codecs.SIMPLE_CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.FLIGHT_TYPE, FlightType.CODEC, FlightType.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, SizeChangeType.CODEC, SizeChangeType.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.MULTIVERSE, Universe.CODEC, Universe.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.MULTIVERSAL_ITEM_VARIANT, MultiversalItemVariant.CODEC, MultiversalItemVariant.CODEC);
        e.dataPackRegistry(PalladiumRegistryKeys.ITEM_UPGRADE, ItemUpgrade.CODEC, ItemUpgrade.CODEC);
    }

}
