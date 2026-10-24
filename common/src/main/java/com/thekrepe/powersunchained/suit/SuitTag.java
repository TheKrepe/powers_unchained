package com.thekrepe.powersunchained.suit;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/**
 * Catalog tags of suits (Suit Creator filters). Only look / origin tags, no ability tags.
 * Colors are ARGB, lang key {@code suit_tag.powers_unchained.<name>}.
 */
public enum SuitTag {
    CLASSIC(0xFFB8322A),
    MODERN(0xFF2F7FB0),
    RETRO(0xFF9A6A3A),
    COMICS(0xFFD9842A),
    MOVIE(0xFF7E4AB0),
    GAMES(0xFF3E9A4A),
    ANIMATION(0xFFC0508A),
    TECH(0xFF2A9AA8),
    FABRIC(0xFF8A7A5A),
    ARMORED(0xFF6A727E),
    STEALTH(0xFF3A3F55),
    STREET(0xFF8A5A3A),
    SPIDER(0xFFA82A3A),
    BRIGHT(0xFFC8A02A),
    DARK(0xFF2E2A33),
    COSMIC(0xFF4A3AA8),
    MYSTIC(0xFF6A2A9A),
    ALTERNATE(0xFF4A8A7A);

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
