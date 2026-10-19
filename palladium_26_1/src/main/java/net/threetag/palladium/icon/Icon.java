package net.threetag.palladium.icon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.registry.PalladiumRegistries;

public interface Icon {

    Codec<Icon> DIRECT_CODEC = PalladiumRegistries.ICON_SERIALIZER.byNameCodec().dispatch(Icon::getSerializer, IconSerializer::codec);

    Codec<Icon> CODEC = Codec.withAlternative(
            DIRECT_CODEC,
            Codec.STRING.comapFlatMap(Icon::read, Icon::toSimpleString).stable()
    );

    StreamCodec<RegistryFriendlyByteBuf, Icon> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Icon decode(RegistryFriendlyByteBuf buf) {
            Identifier id = Identifier.STREAM_CODEC.decode(buf);
            IconSerializer<?> serializer = PalladiumRegistries.ICON_SERIALIZER.getValue(id);
            if (serializer == null) throw new IllegalArgumentException("Unknown icon serializer: " + id);
            return serializer.streamCodec().decode(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Icon value) {
            IconSerializer<?> serializer = value.getSerializer();
            Identifier.STREAM_CODEC.encode(buf, PalladiumRegistries.ICON_SERIALIZER.getKey(serializer));
            encodeUnchecked(buf, serializer, value);
        }

        @SuppressWarnings("unchecked")
        private <T extends Icon> void encodeUnchecked(RegistryFriendlyByteBuf buf, IconSerializer<T> serializer, Icon value) {
            serializer.streamCodec().encode(buf, (T) value);
        }
    };

    static Icon parse(String input) {
        if (input.endsWith(".png")) {
            return new TexturedIcon(Identifier.parse(input));
        } else if (input.startsWith("#")) {
            return new TexturedIcon(TextureReference.parse(input));
        } else {
            Identifier id = Identifier.parse(input);

            if (BuiltInRegistries.ITEM.containsKey(id)) {
                return new ItemIcon(BuiltInRegistries.ITEM.getValue(id));
            }

            return new SpriteIcon(id);
        }
    }

    static DataResult<Icon> read(String path) {
        try {
            return DataResult.success(parse(path));
        } catch (IdentifierException e) {
            return DataResult.error(() -> "Not a valid texture reference: " + path + " " + e.getMessage());
        }
    }

    static String toSimpleString(Icon icon) {
        return switch (icon) {
            case ItemIcon itemIcon -> BuiltInRegistries.ITEM.getKey(itemIcon.itemStackTemplate().item().value()).toString();
            case TexturedIcon(TextureReference texture, java.awt.Color tint) when tint == null -> texture.toString();
            case SpriteIcon(Identifier sprite) -> sprite.toString();
            case null, default -> "invalid";
        };
    }

    IconSerializer<?> getSerializer();

}
