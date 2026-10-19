package net.threetag.palladium.logic.action;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class ActionSerializers {

    public static final DeferredRegister<ActionSerializer<?>> ACTION_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ACTION_SERIALIZER, Palladium.MOD_ID);

    public static final DeferredHolder<ActionSerializer<?>, ConditionalAction.Serializer> CONDITIONAL = ACTION_SERIALIZERS.register("conditional", ConditionalAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, RunCommandAction.Serializer> RUN_COMMAND = ACTION_SERIALIZERS.register("run_command", RunCommandAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetDataAttachmentAction.Serializer> SET_DATA_ATTACHMENT = ACTION_SERIALIZERS.register("set_data_attachment", SetDataAttachmentAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ModifyNumberDataAttachmentAction.Serializer> MODIFY_NUMBER_DATA_ATTACHMENT = ACTION_SERIALIZERS.register("modify_number_data_attachment", ModifyNumberDataAttachmentAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetSizeAction.Serializer> SET_SIZE = ACTION_SERIALIZERS.register("set_size", SetSizeAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ChangeSizeAction.Serializer> CHANGE_SIZE = ACTION_SERIALIZERS.register("change_size", ChangeSizeAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, LeapAction.Serializer> LEAP = ACTION_SERIALIZERS.register("leap", LeapAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, HealAction.Serializer> HEAL = ACTION_SERIALIZERS.register("heal", HealAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetFallDistanceAction.Serializer> SET_FALL_DISTANCE = ACTION_SERIALIZERS.register("set_fall_distance", SetFallDistanceAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, PlaySoundAction.Serializer> PLAY_SOUND = ACTION_SERIALIZERS.register("play_sound", PlaySoundAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetOnFireAction.Serializer> SET_ON_FIRE = ACTION_SERIALIZERS.register("set_on_fire", SetOnFireAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, ExtinguishAction.Serializer> EXTINGUISH = ACTION_SERIALIZERS.register("extinguish", ExtinguishAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, AddSuperpowerAction.Serializer> ADD_SUPERPOWER = ACTION_SERIALIZERS.register("add_superpower", AddSuperpowerAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetSuperpowerAction.Serializer> SET_SUPERPOWER = ACTION_SERIALIZERS.register("set_superpower", SetSuperpowerAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, RemoveSuperpowerAction.Serializer> REMOVE_SUPERPOWER = ACTION_SERIALIZERS.register("remove_superpower", RemoveSuperpowerAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, AddRandomSuperpowerAction.Serializer> ADD_RANDOM_SUPERPOWER = ACTION_SERIALIZERS.register("add_random_superpower", AddRandomSuperpowerAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetRandomSuperpowerAction.Serializer> SET_RANDOM_SUPERPOWER = ACTION_SERIALIZERS.register("set_random_superpower", SetRandomSuperpowerAction.Serializer::new);
    public static final DeferredHolder<ActionSerializer<?>, SetModeAction.Serializer> SET_MODE = ACTION_SERIALIZERS.register("set_mode", SetModeAction.Serializer::new);

}
