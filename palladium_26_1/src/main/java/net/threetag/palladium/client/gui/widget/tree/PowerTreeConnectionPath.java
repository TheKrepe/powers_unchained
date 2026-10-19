package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

public enum PowerTreeConnectionPath implements StringRepresentable {

    STRAIGHT("straight"),
    ELBOW_HORIZONTAL("elbow_horizontal"),
    ELBOW_VERTICAL("elbow_vertical");

    public static final Codec<PowerTreeConnectionPath> CODEC = StringRepresentable.fromEnum(PowerTreeConnectionPath::values);

    private final String name;

    PowerTreeConnectionPath(String name) {
        this.name = name;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.name;
    }
}
