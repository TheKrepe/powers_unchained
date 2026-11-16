package com.star_1mp.powersunleashed.power;

import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Superpowers: a whole set of abilities given / taken at once with {@code /superpower add|remove <power> [targets]}
 * (one system, like Palladium's powers).
 */
public enum Superpower {
    /** Spider-Man: wall climbing, night vision, Spider-Sense, roll (+ the ability wheel to switch them). */
    SPIDERMAN(List.of(PowerAbility.WALL_CLIMBING, PowerAbility.NIGHT_VISION, PowerAbility.SPIDER_SENSE, PowerAbility.ROLL));

    private final List<PowerAbility> abilities;

    Superpower(List<PowerAbility> abilities) {
        this.abilities = abilities;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public List<PowerAbility> abilities() {
        return this.abilities;
    }

    public Component title() {
        return Component.translatable("superpower.powers_unleashed." + this.id());
    }

    public static @Nullable Superpower byId(String id) {
        for (Superpower p : values()) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return null;
    }
}
