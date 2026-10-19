package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

public class SetDataAttachmentAction extends Action {

    public static final MapCodec<SetDataAttachmentAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).fieldOf("attachment").forGetter(a -> a.attachmentId),
            Value.CODEC.fieldOf("value").forGetter(a -> a.value)
    ).apply(instance, SetDataAttachmentAction::new));

    private final ResourceKey<AttachmentType<?>> attachmentId;
    private final Value value;

    public SetDataAttachmentAction(ResourceKey<AttachmentType<?>> attachmentId, Value value) {
        this.attachmentId = attachmentId;
        this.value = value;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean run(DataContext context) {
        var entity = context.getEntity();

        if (entity == null) {
            return false;
        }

        var holder = NeoForgeRegistries.ATTACHMENT_TYPES.get(this.attachmentId).orElse(null);

        if (holder == null) {
            return false;
        }

        var attachmentType = holder.value();
        var current = entity.getData(attachmentType);
        entity.setData((AttachmentType) attachmentType, convertValue(current, context));
        return true;
    }

    private Object convertValue(Object current, DataContext context) {
        if (current instanceof Integer) return this.value.getAsInt(context);
        if (current instanceof Long) return (long) this.value.getAsDouble(context);
        if (current instanceof Float) return this.value.getAsFloat(context);
        if (current instanceof Double) return this.value.getAsDouble(context);
        if (current instanceof Boolean) return this.value.getAsBoolean(context);
        if (current instanceof String) return this.value.getAsString(context);
        return this.value.get(context);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.SET_DATA_ATTACHMENT.get();
    }

    public static class Serializer extends ActionSerializer<SetDataAttachmentAction> {

        @Override
        public MapCodec<SetDataAttachmentAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetDataAttachmentAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Data Attachment")
                    .setDescription("Sets the value of a data attachment on the entity.")
                    .add("attachment", TYPE_ATTACHMENT_TYPE, "The ID of the data attachment to set.")
                    .add("value", TYPE_VALUE, "The value to set the data attachment to.")
                    .addExampleObject(new SetDataAttachmentAction(
                            ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "my_attachment")),
                            new StaticValue(10)
                    ));
        }
    }
}
