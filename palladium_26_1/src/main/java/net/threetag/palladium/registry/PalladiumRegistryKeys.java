package net.threetag.palladium.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
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

public class PalladiumRegistryKeys {

    public static final ResourceKey<Registry<PalladiumEntityDataType<?>>> ENTITY_DATA_TYPE = createRegistryKey("entity_data_type");
    public static final ResourceKey<Registry<AbilitySerializer<?>>> ABILITY_SERIALIZER = createRegistryKey("ability_serializer");
    public static final ResourceKey<Registry<MapCodec<? extends UnlockingHandler>>> ABILITY_UNLOCKING_HANDLER = createRegistryKey("ability_unlocking_handler");
    public static final ResourceKey<Registry<MapCodec<? extends EnablingHandler>>> ABILITY_ENABLING_HANDLER = createRegistryKey("ability_enabling_handler");
    public static final ResourceKey<Registry<KeyBindTypeSerializer<?>>> KEY_BIND_TYPE_SERIALIZER = createRegistryKey("key_bind_type");
    public static final ResourceKey<Registry<ActionSerializer<?>>> ACTION_SERIALIZER = createRegistryKey("action_serializer");
    public static final ResourceKey<Registry<ConditionSerializer<?>>> CONDITION_SERIALIZER = createRegistryKey("condition_serializer");
    public static final ResourceKey<Registry<ValueSerializer<?>>> VALUE_SERIALIZER = createRegistryKey("value_serializer");
    public static final ResourceKey<Registry<CostSerializer<?>>> COST_SERIALIZER = createRegistryKey("cost_serializer");
    public static final ResourceKey<Registry<Power>> POWER = createRegistryKey("power");
    public static final ResourceKey<Registry<PowerProvider>> POWER_PROVIDER = createRegistryKey("power_provider");
    public static final ResourceKey<Registry<PowerDampeningSource>> POWER_DAMPENING_SOURCE = createRegistryKey("power_dampening_source");
    public static final ResourceKey<Registry<IconSerializer<?>>> ICON_SERIALIZER = createRegistryKey("icon_serializer");
    public static final ResourceKey<Registry<MapCodec<? extends Item>>> ITEM_TYPE = createRegistryKey("item_type");
    public static final ResourceKey<Registry<EntityEffect>> ENTITY_EFFECT = createRegistryKey("entity_effect");
    public static final ResourceKey<Registry<CustomizationSerializer<?>>> CUSTOMIZATION_SERIALIZERS = createRegistryKey("customization_serializer");
    public static final ResourceKey<Registry<Customization>> CUSTOMIZATION = createRegistryKey("customization");
    public static final ResourceKey<Registry<CustomizationCategory>> CUSTOMIZATION_CATEGORY = createRegistryKey("customization_category");
    public static final ResourceKey<Registry<FlightTypeSerializer<?>>> FLIGHT_TYPE_SERIALIZERS = createRegistryKey("flight_type_serializer");
    public static final ResourceKey<Registry<FlightType>> FLIGHT_TYPE = createRegistryKey("flight_type");
    public static final ResourceKey<Registry<SizeChangeType>> SIZE_CHANGE_TYPE = createRegistryKey("size_change_type");
    public static final ResourceKey<Registry<Universe>> MULTIVERSE = createRegistryKey("multiverse");
    public static final ResourceKey<Registry<MultiversalItemVariant>> MULTIVERSAL_ITEM_VARIANT = createRegistryKey("multiversal_item_variant");
    public static final ResourceKey<Registry<ItemUpgrade>> ITEM_UPGRADE = createRegistryKey("item_upgrade");

    private static <T> ResourceKey<Registry<T>> createRegistryKey(String name) {
        return ResourceKey.createRegistryKey(Palladium.id(name));
    }

    public static String getPackFolder(ResourceKey<? extends Registry<?>> key) {
        if (key.identifier().getNamespace().equalsIgnoreCase(Identifier.DEFAULT_NAMESPACE)) {
            return key.identifier().getPath();
        } else {
            return key.identifier().getNamespace() + "/" + key.identifier().getPath();
        }
    }

}
