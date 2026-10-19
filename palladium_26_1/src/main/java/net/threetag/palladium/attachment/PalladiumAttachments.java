package net.threetag.palladium.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.Palladium;

public class PalladiumAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Palladium.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> IS_CLIMBING = ATTACHMENT_TYPES.register("is_climbing", () -> AttachmentType.builder(() -> false)
            .sync(ByteBufCodecs.BOOL)
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> CARRIED_TARGET_ID = ATTACHMENT_TYPES.register("carried_target_id", () -> AttachmentType.builder(() -> -1)
            .serialize(Codec.INT.fieldOf("value"))
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> CARRY_THROWABLE = ATTACHMENT_TYPES.register("carry_throwable", () -> AttachmentType.builder(() -> false)
            .serialize(Codec.BOOL.fieldOf("value"))
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> CARRY_THROW_STRENGTH = ATTACHMENT_TYPES.register("carry_throw_strength", () -> AttachmentType.builder(() -> 0.0)
            .serialize(Codec.DOUBLE.fieldOf("value"))
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> CARRY_DISTANCE = ATTACHMENT_TYPES.register("carry_distance", () -> AttachmentType.builder(() -> 2.0)
            .serialize(Codec.DOUBLE.fieldOf("value"))
            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Double>> CARRY_HEIGHT_OFFSET = ATTACHMENT_TYPES.register("carry_height_offset", () -> AttachmentType.builder(() -> 0.0)
            .serialize(Codec.DOUBLE.fieldOf("value"))
            .build());

}
