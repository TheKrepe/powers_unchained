package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum PowerTreeState implements StringRepresentable {

    LOCKED("locked"),
    AVAILABLE("available"),
    UNLOCKED("unlocked");

    public static final Codec<PowerTreeState> CODEC = StringRepresentable.fromEnum(PowerTreeState::values);

    private final String name;

    PowerTreeState(String name) {
        this.name = name;
    }

    public static PowerTreeState of(boolean unlocked, boolean available) {
        return unlocked ? UNLOCKED : available ? AVAILABLE : LOCKED;
    }

    public static PowerTreeState connection(PowerTreeState from, PowerTreeState to) {
        if (from != UNLOCKED) {
            return LOCKED;
        }

        return to == UNLOCKED ? UNLOCKED : AVAILABLE;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name;
    }
}
