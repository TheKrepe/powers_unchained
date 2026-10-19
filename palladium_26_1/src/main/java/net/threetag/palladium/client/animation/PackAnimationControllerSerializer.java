package net.threetag.palladium.client.animation;

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
import net.threetag.palladium.documentation.SettingType;

import java.util.Map;

public abstract class PackAnimationControllerSerializer<T extends PackAnimationController> implements Documented<PackAnimationController, T> {

    private static final BiMap<Identifier, PackAnimationControllerSerializer<?>> TYPES = HashBiMap.create();

    public static final Codec<PackAnimationControllerSerializer<?>> TYPE_CODEC = Identifier.CODEC.flatXmap(identifier -> {
        PackAnimationControllerSerializer<?> serializer = TYPES.get(identifier);
        return serializer != null ? DataResult.success(serializer) : DataResult.error(() -> "Unknown type " + identifier);
    }, serializer -> {
        Identifier identifier = TYPES.inverse().get(serializer);
        return serializer != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown type " + identifier);
    });

    public static <T extends PackAnimationController> PackAnimationControllerSerializer<T> register(Identifier id, PackAnimationControllerSerializer<T> serializer) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Duplicate registration for pack animation controller serializer: " + id);
        }

        TYPES.put(id, serializer);
        return serializer;
    }

    public static Map<Identifier, PackAnimationControllerSerializer<?>> getTypes() {
        return ImmutableMap.copyOf(TYPES);
    }

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<PackAnimationController, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), PackAnimationController.Codecs.SIMPLE_CODEC, provider)
                .addOptional("parent", TYPE_IDENTIFIER, "Another animation controller of the same type to inherit from. Own values override the parent's.")
                .addOptional("animations", SettingType.simple("Map<String, Identifier / Identifier[]>"), "Named animation shortcuts, referenced as \"#name\". A list picks a random entry each time.")
                .addOptional("blend_transition", TYPE_TIME, "Ticks to blend when switching animations. 0 switches instantly.", 5);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<PackAnimationController, T> builder, HolderLookup.Provider provider);

}
