package net.threetag.palladium.logic.value;

import java.util.function.Function;

public interface ValueGetter extends Function<String, Value> {

    default Value withFallback(String key, Value fallback) {
        var value = this.apply(key);
        return value != null ? value : fallback;
    }

}
