package net.threetag.palladium.logic.action;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.SuperpowerUtil;

public class AddSuperpowerAction extends Action {

    public static final MapCodec<AddSuperpowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Power.HOLDER_CODEC.fieldOf("power").forGetter(a -> a.superpower)
    ).apply(instance, AddSuperpowerAction::new));

    public final Holder<Power> superpower;

    public AddSuperpowerAction(Holder<Power> superpower) {
        this.superpower = superpower;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        return SuperpowerUtil.addSuperpower(entity, this.superpower);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.ADD_SUPERPOWER.get();
    }

    public static class Serializer extends ActionSerializer<AddSuperpowerAction> {

        @Override
        public MapCodec<AddSuperpowerAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, AddSuperpowerAction> builder, HolderLookup.Provider provider) {
            builder.setName("Add Superpower")
                    .setDescription("Adds the given superpower to the entity.")
                    .add("power", TYPE_POWER, "The ID of the superpower to add.");
            var example = new JsonObject();
            example.addProperty("type", ActionSerializers.ADD_SUPERPOWER.getId().toString());
            example.addProperty("power", "example:my_power");
            builder.addExampleJson(example);
        }
    }
}
