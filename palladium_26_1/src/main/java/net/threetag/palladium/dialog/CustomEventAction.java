package net.threetag.palladium.dialog;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.action.Action;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;

public record CustomEventAction(Identifier id, Optional<CompoundTag> data) implements Action {

    public static final String TAG_ID = "id";
    public static final String TAG_DATA = "data";

    public static final MapCodec<CustomEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf(TAG_ID).forGetter(CustomEventAction::id),
            CompoundTag.CODEC.optionalFieldOf(TAG_DATA).forGetter(CustomEventAction::data)
    ).apply(instance, CustomEventAction::new));

    @Override
    public MapCodec<? extends Action> codec() {
        return CODEC;
    }

    @Override
    public Optional<ClickEvent> createAction(@NonNull Map<String, ValueGetter> valueGetters) {
        CompoundTag payload = new CompoundTag();
        payload.putString(TAG_ID, this.id.toString());
        this.data.ifPresent(tag -> payload.put(TAG_DATA, tag));
        return Optional.of(new ClickEvent.Custom(PalladiumDialogActions.CUSTOM_EVENT.getId(), Optional.of(payload)));
    }

}
