package net.threetag.palladium.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;

public class ExtinguishAction extends Action {

    public static final MapCodec<ExtinguishAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("no_effects", false).forGetter(a -> a.noEffects)
    ).apply(instance, ExtinguishAction::new));

    public final boolean noEffects;

    public ExtinguishAction(boolean noEffects) {
        this.noEffects = noEffects;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        if (noEffects) {
            entity.clearFire();
        } else {
            entity.extinguishFire();
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.EXTINGUISH.get();
    }

    public static class Serializer extends ActionSerializer<ExtinguishAction> {

        @Override
        public MapCodec<ExtinguishAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, ExtinguishAction> builder, HolderLookup.Provider provider) {
            builder.setName("Extinguish")
                    .setDescription("Clears fire from the entity.")
                    .addOptional("no_effects", TYPE_BOOLEAN, "If true, clears the fire without additional effects like sounds.", false)
                    .addExampleObject(new ExtinguishAction(false));
        }
    }

}