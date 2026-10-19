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

public class SetRandomSuperpowerAction extends Action {

    public static final MapCodec<SetRandomSuperpowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumHolderSet.codec(PalladiumRegistryKeys.POWER).fieldOf("powers").forGetter(a -> a.superpowers)
    ).apply(instance, SetRandomSuperpowerAction::new));

    public final PalladiumHolderSet<Power> superpowers;

    public SetRandomSuperpowerAction(PalladiumHolderSet<Power> superpowers) {
        this.superpowers = superpowers;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getLivingEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        var powers = this.superpowers.resolve(entity.registryAccess());

        if (powers.isEmpty()) {
            return false;
        }

        SuperpowerUtil.setSuperpower(entity, powers.get(entity.getRandom().nextInt(powers.size())));
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.SET_RANDOM_SUPERPOWER.get();
    }

    public static class Serializer extends ActionSerializer<SetRandomSuperpowerAction> {

        @Override
        public MapCodec<SetRandomSuperpowerAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, SetRandomSuperpowerAction> builder, HolderLookup.Provider provider) {
            builder.setName("Set Random Superpower")
                    .setDescription("Removes all superpowers from the entity and sets it to a random one from the given list.")
                    .add("powers", TYPE_POWER_HOLDER_SET, "IDs or tags of powers to randomly pick from.");

            var example = new JsonObject();
            example.addProperty("type", ActionSerializers.SET_RANDOM_SUPERPOWER.getId().toString());
            example.addProperty("power", "example:my_power");
            builder.addExampleJson(example);

            example = new JsonObject();
            example.addProperty("type", ActionSerializers.SET_RANDOM_SUPERPOWER.getId().toString());
            var list = new JsonArray();
            list.add("example:my_power");
            list.add("example:my_second_power");
            example.add("power", list);
            builder.addExampleJson(example);

            example = new JsonObject();
            example.addProperty("type", ActionSerializers.SET_RANDOM_SUPERPOWER.getId().toString());
            example.addProperty("power", "#example:my_power_tag");
            builder.addExampleJson(example);
        }
    }
}
