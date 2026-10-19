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
import net.threetag.palladium.util.NumberOperation;

public class ModifyNumberDataAttachmentAction extends Action {

    public static final MapCodec<ModifyNumberDataAttachmentAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).fieldOf("attachment").forGetter(a -> a.attachmentId),
            NumberOperation.CODEC.fieldOf("operation").forGetter(a -> a.operation),
            Value.CODEC.fieldOf("value").forGetter(a -> a.value)
    ).apply(instance, ModifyNumberDataAttachmentAction::new));

    private final ResourceKey<AttachmentType<?>> attachmentId;
    private final NumberOperation operation;
    private final Value value;

    public ModifyNumberDataAttachmentAction(ResourceKey<AttachmentType<?>> attachmentId, NumberOperation operation, Value value) {
        this.attachmentId = attachmentId;
        this.operation = operation;
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

        if (!(current instanceof Number currentNum)) {
            return false;
        }

        var operand = this.value.getAsNumber(context);
        entity.setData((AttachmentType) attachmentType, this.operation.apply(currentNum, operand));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.MODIFY_NUMBER_DATA_ATTACHMENT.get();
    }

    public static class Serializer extends ActionSerializer<ModifyNumberDataAttachmentAction> {

        @Override
        public MapCodec<ModifyNumberDataAttachmentAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, ModifyNumberDataAttachmentAction> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Number Data Attachment")
                    .setDescription("Applies an arithmetic operation to a numeric data attachment on the entity.")
                    .add("attachment", TYPE_ATTACHMENT_TYPE, "The ID of the data attachment to modify.")
                    .add("operation", TYPE_NUMBER_OPERATION, "The arithmetic operation to apply.")
                    .add("value", TYPE_VALUE, "The operand value.")
                    .addExampleObject(new ModifyNumberDataAttachmentAction(
                            ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "my_attachment")),
                            NumberOperation.ADD,
                            new StaticValue(5)
                    ));
        }
    }
}
