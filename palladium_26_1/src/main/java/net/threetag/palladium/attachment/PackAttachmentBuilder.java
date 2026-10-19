package net.threetag.palladium.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.NotNull;

public final class PackAttachmentBuilder<T> {

    public static final Codec<PackAttachmentBuilder<?>> CODEC = PackAttachmentCodecs.TYPE_CODEC
            .dispatch("type", PackAttachmentBuilder::codec, PackAttachmentBuilder::mapCodec);

    static <T> MapCodec<PackAttachmentBuilder<T>> mapCodec(PackAttachmentCodecs.Entry<T> entry) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                entry.codec().optionalFieldOf("default", entry.defaultSupplier().get()).forGetter(PackAttachmentBuilder::defaultValue),
                SyncWith.CODEC.fieldOf("sync_with").forGetter(PackAttachmentBuilder::syncWith),
                Codec.BOOL.optionalFieldOf("copy_on_death", true).forGetter(PackAttachmentBuilder::copyOnDeath)
        ).apply(instance, (defaultValue, sync, copy) -> new PackAttachmentBuilder<>(entry, defaultValue, sync, copy)));
    }

    private final PackAttachmentCodecs.Entry<T> codec;
    private final T defaultValue;
    private final SyncWith syncWith;
    private final boolean copyOnDeath;
    private AttachmentType<T> type;

    public PackAttachmentBuilder(PackAttachmentCodecs.Entry<T> codec, T defaultValue, SyncWith syncWith, boolean copyOnDeath) {
        this.codec = codec;
        this.defaultValue = defaultValue;
        this.syncWith = syncWith;
        this.copyOnDeath = copyOnDeath;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public AttachmentType<T> build(Identifier id) {
        Codec<T> codec = this.codec.codec();
        AttachmentType.Builder<T> builder = AttachmentType.builder(() -> defaultValue).serialize(codec.fieldOf(id.toString()));

        if (this.syncWith != SyncWith.NONE) {
            StreamCodec streamCodec = this.codec.streamCodec();
            builder.sync((holder, player) -> holder == player || this.syncWith() == SyncWith.ALL, streamCodec);
        }

        if (this.copyOnDeath) {
            builder.copyOnDeath();
        }

        return this.type = builder.build();
    }

    public AttachmentType<T> getType() {
        return type;
    }

    public PackAttachmentCodecs.Entry<T> codec() {
        return codec;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public SyncWith syncWith() {
        return syncWith;
    }

    public boolean copyOnDeath() {
        return copyOnDeath;
    }

    public enum SyncWith implements StringRepresentable {

        NONE("none"),
        SELF("self"),
        ALL("all");

        public static final Codec<SyncWith> CODEC = StringRepresentable.fromEnum(SyncWith::values);

        private final String name;

        SyncWith(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }

}
