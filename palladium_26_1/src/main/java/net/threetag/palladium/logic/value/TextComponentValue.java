package net.threetag.palladium.logic.value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.molang.EntityContext;
import net.threetag.palladium.logic.molang.MoLangQueryRegistry;
import net.threetag.palladium.util.molang.ModifyStringFunction;
import team.unnamed.mocha.MochaEngine;

public abstract class TextComponentValue extends Value implements EntityContext {

    public final String molang;
    public final ModifyStringFunction function;
    private Entity cachedEntity;

    public TextComponentValue(String molang) {
        this.molang = molang;

        if (this.molang != null && !this.molang.isEmpty() && !this.molang.isBlank()) {
            MochaEngine<?> mocha = MoLangQueryRegistry.create(this);
            this.function = mocha.compile(this.molang, ModifyStringFunction.class);
        } else {
            this.function = null;
        }
    }

    @Override
    public Object get(DataContext context) {
        Component component = this.getComponent(context);

        if (this.function != null) {
            this.cachedEntity = context.getEntity();

            if (this.cachedEntity != null) {
                component = Component.literal(this.function.modify(component.getString())).setStyle(component.getStyle());
            }
        }

        return component;
    }

    @Override
    public Entity entity() {
        return this.cachedEntity;
    }

    public abstract Component getComponent(DataContext context);

    protected static <B extends TextComponentValue> RecordCodecBuilder<B, String> modifyFunctionCodec() {
        return Codec.STRING.optionalFieldOf("modify", "").forGetter(v -> v.molang);
    }

    public static abstract class ComponentSerializer<T extends TextComponentValue> extends ValueSerializer<T> {

        @Override
        public SettingType getReturnType() {
            return TYPE_TEXT_COMPONENT;
        }

        @Override
        public CodecDocumentationBuilder<Value, T> getDocumentation(HolderLookup.Provider provider) {
            return super.getDocumentation(provider).addOptional("modify", TYPE_MOLANG, "MoLang function to modify the value.");
        }
    }
}
