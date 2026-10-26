package com.star_1mp.powersunchained.suit;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * Catalog tags of suits (Suit Creator filters): publisher, universe and origin (owner's list), no ability tags.
 * Colors are ARGB, lang key {@code suit_tag.powers_unchained.<name>}.
 */
public enum SuitTag {
    MARVEL(0xFFC8282E),
    DC(0xFF2462C8),
    FIRST_APPEARANCE(0xFFB8862A),
    MOVIE(0xFF7E4AB0),
    DCU(0xFF2A8AB8),
    DCEU(0xFF4A5468),
    MCU(0xFF9A1E2A),
    GAMES(0xFF3E9A4A),
    COMICS(0xFFD9842A),
    ANIMATION(0xFFC0508A),
    MARVEL_MIDNIGHT(0xFF5A2A7A);

    private final int color;

    SuitTag(int color) {
        this.color = color;
    }

    public int color() {
        return this.color;
    }

    public String key() {
        return "suit_tag.powers_unchained." + this.name().toLowerCase(Locale.ROOT);
    }

    public Component title() {
        return Component.translatable(this.key());
    }
}
