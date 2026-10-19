package net.threetag.palladium.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.Map;

public class DelayedDataComponentMap {

    public static final Codec<DelayedDataComponentMap> CODEC = Codec.unboundedMap(DataComponentType.PERSISTENT_CODEC, Codec.PASSTHROUGH)
            .xmap(DelayedDataComponentMap::new, codec -> codec.values);

    public static final DelayedDataComponentMap EMPTY = new DelayedDataComponentMap(Collections.emptyMap());

    private final Map<DataComponentType<?>, Dynamic<?>> values;

    DelayedDataComponentMap(Map<DataComponentType<?>, Dynamic<?>> values) {
        this.values = values;
    }

    @SuppressWarnings({"rawtypes", "UnnecessaryLocalVariable", "unchecked"})
    public void addToProperties(Item.Properties properties) {
        this.values.forEach((type, dynamic) -> {
            DataComponentType type1 = type;
            var value = dynamic.getValue();

            if (value instanceof JsonElement jsonElement) {
                properties.delayedComponent(type1, context -> type.codecOrThrow().parse(context.createSerializationContext(JsonOps.INSTANCE), jsonElement).getOrThrow(JsonParseException::new));
            }
        });
    }

}
