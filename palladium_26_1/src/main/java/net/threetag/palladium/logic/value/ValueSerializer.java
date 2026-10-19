package net.threetag.palladium.logic.value;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;
import net.threetag.palladium.documentation.SettingType;

public abstract class ValueSerializer<T extends Value> implements Documented<Value, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<Value, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), Value.CODEC, provider);
        builder.setReturnType(this.getReturnType());
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract SettingType getReturnType();

    public abstract void addDocumentation(CodecDocumentationBuilder<Value, T> builder, HolderLookup.Provider provider);

}
