package net.threetag.palladium.logic.cost;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.SpriteIcon;
import net.threetag.palladium.util.NumberOperation;
import org.jetbrains.annotations.Nullable;

public class NumberDataAttachmentCost extends Cost {

    public static final MapCodec<NumberDataAttachmentCost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).fieldOf("attachment").forGetter(c -> c.attachmentId),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(c -> c.amount),
            Icon.CODEC.fieldOf("icon").forGetter(c -> c.icon),
            ComponentSerialization.CODEC.fieldOf("description").forGetter(c -> c.description)
    ).apply(instance, NumberDataAttachmentCost::new));

    private final ResourceKey<AttachmentType<?>> attachmentId;
    private final int amount;
    private final Icon icon;
    private final Component description;

    public NumberDataAttachmentCost(ResourceKey<AttachmentType<?>> attachmentId, int amount, Icon icon, Component description) {
        this.attachmentId = attachmentId;
        this.amount = amount;
        this.icon = icon;
        this.description = description;
    }

    @Override
    public boolean hasEnoughCurrency(LivingEntity entity) {
        var current = this.getCurrentValue(entity);
        return current != null && current.doubleValue() >= this.amount;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void consumeCurrency(LivingEntity entity) {
        var attachmentType = NeoForgeRegistries.ATTACHMENT_TYPES.get(this.attachmentId).orElse(null);
        var current = this.getCurrentValue(entity);

        if (attachmentType == null || current == null || current.doubleValue() < this.amount) {
            return;
        }

        entity.setData((AttachmentType) attachmentType.value(), NumberOperation.SUBTRACT.apply(current, this.amount));
    }

    @Nullable
    private Number getCurrentValue(LivingEntity entity) {
        var attachmentType = NeoForgeRegistries.ATTACHMENT_TYPES.get(this.attachmentId).orElse(null);

        if (attachmentType == null) {
            return null;
        }

        return entity.getData(attachmentType.value()) instanceof Number number ? number : null;
    }

    @Override
    public CostDisplay createDisplay() {
        return new CostDisplay(this.icon, this.amount, this.description);
    }

    @Override
    public CostSerializer<?> getSerializer() {
        return CostSerializers.NUMBER_DATA_ATTACHMENT.value();
    }

    public static class Serializer extends CostSerializer<NumberDataAttachmentCost> {

        @Override
        public MapCodec<NumberDataAttachmentCost> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Cost, NumberDataAttachmentCost> builder, HolderLookup.Provider provider) {
            builder.setName("Number Data Attachment Cost").setDescription("Requires the entity to have a certain value in a number data attachment, which is consumed.")
                    .add("attachment", TYPE_ATTACHMENT_TYPE, "The ID of the data attachment.")
                    .addOptional("amount", TYPE_POSITIVE_INT, "The amount consumed from the data attachment.", 1)
                    .add("icon", TYPE_ICON, "The icon shown in the UI.")
                    .add("description", TYPE_TEXT_COMPONENT, "The display text shown in the UI.")
                    .addExampleObject(new NumberDataAttachmentCost(
                            ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Identifier.fromNamespaceAndPath("example", "number_attachment_id")),
                            10,
                            new SpriteIcon(Identifier.parse("example:sprite_path")),
                            Component.literal("My Currency")
                    ));
        }
    }
}
