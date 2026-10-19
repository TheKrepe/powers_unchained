package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.FloatValueHolder;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumCodecs;

public class SetSizeAction extends Action {

    public static final MapCodec<SetSizeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryFixedCodec.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE).fieldOf("size_change_type").forGetter(a -> a.sizeChangeType),
            PalladiumCodecs.POSITIVE_FLOAT_DYNAMIC.fieldOf("size").forGetter(a -> a.size)
    ).apply(instance, SetSizeAction::new));

    public final Holder<SizeChangeType> sizeChangeType;
    public final FloatValueHolder size;

    public SetSizeAction(Holder<SizeChangeType> sizeChangeType, FloatValueHolder size) {
        this.sizeChangeType = sizeChangeType;
        this.size = size;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();

        if (!(entity instanceof LivingEntity living) || entity.level().isClientSide()) {
            return false;
        }

        var sizeData = PalladiumEntityData.get(living, PalladiumEntityDataTypes.SIZE_CHANGE.get());

        if (sizeData == null) {
            return false;
        }

        sizeData.setSize(this.sizeChangeType.value().clamp(this.size.get(context)));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.SET_SIZE.get();
    }

    public static class Serializer extends ActionSerializer<SetSizeAction> {

        @Override
        public MapCodec<SetSizeAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetSizeAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Size")
                    .setDescription("Instantly sets the entity's size, clearing all active size modifiers. The size change type is used for min/max clamping.")
                    .add("size_change_type", TYPE_IDENTIFIER, "The size change type whose min/max bounds are used to clamp the size.")
                    .add("size", TYPE_FLOAT_VALUE, "The target size to set.")
                    .addExampleObject(new SetSizeAction(
                            provider.holderOrThrow(PalladiumSizeChangeTypes.DEFAULT),
                            new FloatValueHolder(2.0F)
                    ));
        }
    }
}
