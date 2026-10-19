package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.Documented;

public abstract class ActionSerializer<T extends Action> implements Documented<Action, T> {

    public abstract MapCodec<T> codec();

    @Override
    public CodecDocumentationBuilder<Action, T> getDocumentation(HolderLookup.Provider provider) {
        var builder = new CodecDocumentationBuilder<>(codec(), Action.CODEC, provider);
        this.addDocumentation(builder, provider);
        return builder;
    }

    public abstract void addDocumentation(CodecDocumentationBuilder<Action, T> builder, HolderLookup.Provider provider);

}
