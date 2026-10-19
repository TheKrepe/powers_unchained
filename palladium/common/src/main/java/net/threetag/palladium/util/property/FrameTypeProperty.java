package net.threetag.palladium.util.property;

import net.minecraft.advancements.FrameType;

import java.util.Locale;

public class FrameTypeProperty extends EnumPalladiumProperty<FrameType> {
    public FrameTypeProperty(String key) {
        super(key);
    }

    @Override
    public FrameType[] getValues() {
        return FrameType.values();
    }

    @Override
    public String getNameFromEnum(FrameType value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
