package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.threetag.palladium.customization.BuiltinCustomization;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.datagen.internal.PalladiumCustomizationProvider;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumHolderSet;

public class CustomizationSelectedCondition implements Condition {

    public static final MapCodec<CustomizationSelectedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumHolderSet.codec(PalladiumRegistryKeys.CUSTOMIZATION).fieldOf("customization").forGetter(c -> c.customization)
    ).apply(instance, CustomizationSelectedCondition::new));

    private final PalladiumHolderSet<Customization> customization;

    public CustomizationSelectedCondition(PalladiumHolderSet<Customization> customization) {
        this.customization = customization;
    }

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity != null) {
            var handler = EntityCustomizationHandler.get(entity);

            for (Holder<Customization> holder : this.customization.resolve(entity.registryAccess())) {
                if (handler.isSelected(holder)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return ConditionSerializers.CUSTOMIZATION_SELECTED.get();
    }

    public static class Serializer extends ConditionSerializer<CustomizationSelectedCondition> {

        @Override
        public MapCodec<CustomizationSelectedCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, CustomizationSelectedCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Customization Selected")
                    .setDescription("Checks if the given customization is currently selected by the entity.")
                    .add("customization", TYPE_CUSTOMIZATION_HOLDER_SET, "ID(s) or tag of the required customization.")
                    .addExampleObject(new CustomizationSelectedCondition(
                            PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(BuiltinCustomization.Type.LUCRAFT_ARC_REACTOR.getResourceKey()))))
                    );
        }
    }
}
