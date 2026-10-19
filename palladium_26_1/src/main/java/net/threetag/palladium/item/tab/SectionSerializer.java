package net.threetag.palladium.item.tab;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.mcexpanded.fancytabsections.Section.Section;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

import java.util.Map;

public abstract class SectionSerializer<T extends Section<?>> implements Documented<Section<?>, T> {

    private static final BiMap<Identifier, SectionSerializer<?>> TYPES = HashBiMap.create();
    private static final BiMap<Class<? extends Section<?>>, SectionSerializer<?>> CLAZZ_TYPES = HashBiMap.create();

    public static final Codec<SectionSerializer<?>> TYPE_CODEC = Identifier.CODEC.flatXmap(identifier -> {
        SectionSerializer<?> serializer = TYPES.get(identifier);
        return serializer != null ? DataResult.success(serializer) : DataResult.error(() -> "Unknown type " + identifier);
    }, serializer -> {
        Identifier identifier = TYPES.inverse().get(serializer);
        return serializer != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown type " + identifier);
    });
    public static Codec<Section<?>> CODEC = TYPE_CODEC.dispatch(section -> CLAZZ_TYPES.get(section.getClass()), SectionSerializer::codec);

    public static <T extends Section<?>> SectionSerializer<T> register(Identifier id, Class<? extends Section<?>> clazz, SectionSerializer<T> serializer) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Duplicate registration for section serializer: " + id);
        }

        TYPES.put(id, serializer);
        CLAZZ_TYPES.put(clazz, serializer);
        return serializer;
    }

    public static Map<Identifier, SectionSerializer<?>> getTypes() {
        return ImmutableMap.copyOf(TYPES);
    }

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<Section<?>, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<Section<?>, T> builder, HolderLookup.Provider provider);

}
