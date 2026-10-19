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
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class SetSuperpowerAction extends Action {

    public static final MapCodec<SetSuperpowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Power.HOLDER_CODEC.fieldOf("power").forGetter(a -> a.superpower)
    ).apply(instance, SetSuperpowerAction::new));

    public final Holder<Power> superpower;

    public SetSuperpowerAction(Holder<Power> superpower) {
        this.superpower = superpower;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        SuperpowerUtil.setSuperpower(entity, this.superpower);
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.SET_SUPERPOWER.get();
    }

    public static class Serializer extends ActionSerializer<SetSuperpowerAction> {

        @Override
        public MapCodec<SetSuperpowerAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetSuperpowerAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Superpower")
                    .setDescription("Removes all superpowers from the entity and sets it to just the given one.")
                    .add("power", TYPE_POWER, "The ID of the superpower to set.");

            var example = new JsonObject();
            example.addProperty("type", ActionSerializers.SET_SUPERPOWER.getId().toString());
            example.addProperty("power", "example:my_power");
            builder.addExampleJson(example);
        }
    }
}
