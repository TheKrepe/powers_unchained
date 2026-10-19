package net.threetag.palladium.logic.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.SuperpowerUtil;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.tag.PalladiumPowerTags;
import net.threetag.palladium.util.PalladiumHolderSet;

public class RemoveSuperpowerAction extends Action {

    public static final MapCodec<RemoveSuperpowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumHolderSet.codec(PalladiumRegistryKeys.POWER).fieldOf("powers").forGetter(a -> a.superpowers)
    ).apply(instance, RemoveSuperpowerAction::new));

    public final PalladiumHolderSet<Power> superpowers;

    public RemoveSuperpowerAction(PalladiumHolderSet<Power> superpowers) {
        this.superpowers = superpowers;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        return SuperpowerUtil.removeSuperpower(entity, this.superpowers::contains);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.REMOVE_SUPERPOWER.get();
    }

    public static class Serializer extends ActionSerializer<RemoveSuperpowerAction> {

        @Override
        public MapCodec<RemoveSuperpowerAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, RemoveSuperpowerAction> builder, HolderLookup.Provider provider) {
            builder.setName("Remove Superpower")
                    .setDescription("Removes the given superpowers from the entity.")
                    .add("powers", TYPE_POWER_HOLDER_SET, "IDs or tags of superpowers to remove.");

            var example = new JsonObject();
            example.addProperty("type", ActionSerializers.REMOVE_SUPERPOWER.getId().toString());
            example.addProperty("power", "example:my_power");
            builder.addExampleJson(example);

            example = new JsonObject();
            example.addProperty("type", ActionSerializers.REMOVE_SUPERPOWER.getId().toString());
            var list = new JsonArray();
            list.add("example:my_power");
            list.add("example:my_second_power");
            example.add("power", list);
            builder.addExampleJson(example);

            example = new JsonObject();
            example.addProperty("type", ActionSerializers.REMOVE_SUPERPOWER.getId().toString());
            example.addProperty("power", "#example:my_power_tag");
            builder.addExampleJson(example);
        }
    }
}
