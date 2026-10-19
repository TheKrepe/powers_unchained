package net.threetag.palladium.logic.cost;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class CostSerializer<T extends Cost> implements Documented<Cost, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<Cost, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), Cost.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<Cost, T> builder, HolderLookup.Provider provider);

}
