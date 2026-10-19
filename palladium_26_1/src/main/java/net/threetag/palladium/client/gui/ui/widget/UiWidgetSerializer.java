package net.threetag.palladium.client.gui.ui.widget;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

import java.util.Map;

public abstract class UiWidgetSerializer<T extends UiWidget> implements Documented<UiWidget, T> {

    private static final BiMap<Identifier, UiWidgetSerializer<?>> TYPES = HashBiMap.create();

    public static final Codec<UiWidgetSerializer<?>> TYPE_CODEC = Identifier.CODEC.flatXmap(identifier -> {
        UiWidgetSerializer<?> serializer = TYPES.get(identifier);
        return serializer != null ? DataResult.success(serializer) : DataResult.error(() -> "Unknown type " + identifier);
    }, serializer -> {
        Identifier identifier = TYPES.inverse().get(serializer);
        return serializer != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown type " + identifier);
    });

    public static <T extends UiWidget> UiWidgetSerializer<T> register(Identifier id, UiWidgetSerializer<T> serializer) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Duplicate registration for UI widget serializer: " + id);
        }

        TYPES.put(id, serializer);
        return serializer;
    }

    public static Map<Identifier, UiWidgetSerializer<?>> getTypes() {
        return ImmutableMap.copyOf(TYPES);
    }

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<UiWidget, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), UiWidget.CODEC, provider)
                .addOptional("properties", TYPE_UI_PROPERTIES, "Properties of this widget", UiWidgetProperties.DEFAULT);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<UiWidget, T> builder, HolderLookup.Provider provider);
}
