package net.threetag.palladium.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public record BooleanDataAttachmentCondition(ResourceKey<AttachmentType<?>> attachmentId, boolean fallback,
                                             boolean expected) implements Condition {

    public static final MapCodec<BooleanDataAttachmentCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    ResourceKey.codec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).fieldOf("attachment").forGetter(BooleanDataAttachmentCondition::attachmentId),
                    Codec.BOOL.optionalFieldOf("fallback", false).forGetter(BooleanDataAttachmentCondition::fallback),
                    Codec.BOOL.optionalFieldOf("expected", true).forGetter(BooleanDataAttachmentCondition::expected)
            ).apply(instance, BooleanDataAttachmentCondition::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BooleanDataAttachmentCondition> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES), BooleanDataAttachmentCondition::attachmentId,
            ByteBufCodecs.BOOL, BooleanDataAttachmentCondition::fallback,
            ByteBufCodecs.BOOL, BooleanDataAttachmentCondition::expected,
            BooleanDataAttachmentCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        var attachmentType = NeoForgeRegistries.ATTACHMENT_TYPES.get(this.attachmentId).orElse(null);
        var value = this.fallback;

        if (entity != null && attachmentType != null) {
            var data = entity.getExistingDataOrNull(attachmentType.value());

            if (data instanceof Boolean b) {
                value = b;
            } else if (data instanceof Number n) {
                value = n.doubleValue() > 0;
            }
        }

        return value == this.expected;
    }

    @Override
    public ConditionSerializer<BooleanDataAttachmentCondition> getSerializer() {
        return ConditionSerializers.BOOLEAN_DATA_ATTACHMENT.get();
    }

    public static class Serializer extends ConditionSerializer<BooleanDataAttachmentCondition> {

        @Override
        public MapCodec<BooleanDataAttachmentCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, BooleanDataAttachmentCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Boolean Data Attachment")
                    .setDescription("Checks if the value of a boolean data attachment matches the expected value.")
                    .add("attachment", TYPE_ATTACHMENT_TYPE, "The ID of the data attachment.")
                    .addOptional("fallback", TYPE_BOOLEAN, "The value that is used if the data attachment is not present.")
                    .addOptional("expected", TYPE_BOOLEAN, "The value the data attachment must have for the condition to succeed.")
                    .addExampleObject(new BooleanDataAttachmentCondition(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "boolean_attachment_id")), false, true));
        }
    }
}
