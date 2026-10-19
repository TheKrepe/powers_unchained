package net.threetag.palladium.logic.action;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistries;

import java.util.List;

public abstract class Action {

    public static final Codec<Action> CODEC = PalladiumRegistries.ACTION_SERIALIZER.byNameCodec().dispatch(Action::getSerializer, ActionSerializer::codec);
    public static final Codec<List<Action>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);

    public abstract boolean run(DataContext context);

    public abstract ActionSerializer<?> getSerializer();

    public static boolean runList(List<Action> actions, DataContext context) {
        boolean result = false;
        for (Action action : actions) {
            result |= action.run(context);
        }
        return result;
    }
}
