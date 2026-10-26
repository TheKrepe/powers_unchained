package com.star_1mp.powersunchained.power;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The abilities superpowers are made of. Switchable ones show in the ability wheel and are switched on / off there;
 * the others work whenever the power is owned.
 */
public enum PowerAbility {
    /** Crawl over walls and ceilings like Marvel's Spider-Man. */
    WALL_CLIMBING(true, true),
    /** Night vision (off until switched on in the wheel). */
    NIGHT_VISION(true, false),
    /** Warns about danger nearby. */
    SPIDER_SENSE(true, true),
    /** Dodge roll on the ground, front flip in the air. */
    ROLL(false, true);

    private final boolean toggleable;
    private final boolean onByDefault;

    PowerAbility(boolean toggleable, boolean onByDefault) {
        this.toggleable = toggleable;
        this.onByDefault = onByDefault;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public boolean toggleable() {
        return this.toggleable;
    }

    public boolean onByDefault() {
        return this.onByDefault;
    }

    public Component title() {
        return Component.translatable("superpower.powers_unchained." + this.id());
    }

    public static @Nullable PowerAbility byId(String id) {
        for (PowerAbility a : values()) {
            if (a.id().equals(id)) {
                return a;
            }
        }
        return null;
    }
}
