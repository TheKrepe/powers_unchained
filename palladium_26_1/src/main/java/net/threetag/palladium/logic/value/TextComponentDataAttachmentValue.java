package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class TextComponentDataAttachmentValue extends TextComponentValue {

    public static final MapCodec<TextComponentDataAttachmentValue> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).fieldOf("attachment").forGetter(v -> v.attachmentId),
            ComponentSerialization.CODEC.optionalFieldOf("fallback", Component.empty()).forGetter(v -> v.fallback),
            modifyFunctionCodec()
    ).apply(instance, TextComponentDataAttachmentValue::new));

    private final ResourceKey<AttachmentType<?>> attachmentId;
    private final Component fallback;

    public TextComponentDataAttachmentValue(ResourceKey<AttachmentType<?>> attachmentId, Component fallback, String molang) {
        super(molang);
        this.attachmentId = attachmentId;
        this.fallback = fallback;
    }

    @Override
    public Component getComponent(DataContext context) {
        var entity = context.getEntity();
        var attachmentType = NeoForgeRegistries.ATTACHMENT_TYPES.get(this.attachmentId).orElse(null);

        if (entity == null || attachmentType == null) {
            return this.fallback;
        }

        var data = entity.getExistingDataOrNull(attachmentType.value());

        if (data == null) {
            return this.fallback;
        }

        return data instanceof Component component ? component : Component.literal(data.toString());
    }

    @Override
    public ValueSerializer<?> getSerializer() {
        return ValueSerializers.TEXT_COMPONENT_DATA_ATTACHMENT.get();
    }

    public static class Serializer extends ComponentSerializer<TextComponentDataAttachmentValue> {

        @Override
        public MapCodec<TextComponentDataAttachmentValue> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Value, TextComponentDataAttachmentValue> builder, HolderLookup.Provider provider) {
            builder.setName("Text Component Data Attachment").setDescription("Returns the value of a data attachment as a text component.")
                    .add("attachment", TYPE_ATTACHMENT_TYPE, "The ID of the data attachment.")
                    .addOptional("fallback", TYPE_TEXT_COMPONENT, "If the data attachment doesn't exist or is not existent in the entity, this fallback value will be used.")
                    .addExampleObject(new TextComponentDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "component_attachment_id")), Component.empty(), ""))
                    .addExampleObject(new TextComponentDataAttachmentValue(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "component_attachment_id")), Component.literal("example_fallback"), ""));
        }
    }
}
