package com.thekrepe.powersunchained.client.ability;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Small, vanilla-styled list of the ability keys (position, size, text opacity, names and on/off set by the player
 * in {@link AbilityHudConfigScreen}) at the left edge of the screen, shown whenever the player has any
 * ability (suit power, gadget...): each key in a small dark box with the ability on it next to it (bright when the
 * ability is on, grey when off); free keys only show their dimmed box.
 * <p>
 * Abilities come from {@link Provider}s, so new powers only register a provider.
 */
public final class AbilityHud {

    /**
     * One ability on a key.
     *
     * @param extra short value after the name (e.g. ammo), or {@code null}
     */
    public record Entry(Component name, @Nullable Component extra, boolean enabled) {
    }

    /** Supplies the ability of a key slot (0-based) for the player, or {@code null} if it has none there. */
    public interface Provider {
        @Nullable Entry get(LocalPlayer player, int slot);
    }

    private static final List<Provider> PROVIDERS = new ArrayList<>();
    private static final int ROW_HEIGHT = 12;

    private AbilityHud() {
    }

    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        AbilityHudConfig config = AbilityHudConfig.get();
        if (player == null || player.isSpectator() || !config.enabled || mc.gui.screen() instanceof AbilityHudConfigScreen) {
            return;
        }
        Entry[] entries = collect(player);
        if (entries == null) {
            return;
        }
        int[] pos = position(mc.font, entries, config, graphics.guiWidth(), graphics.guiHeight());
        draw(graphics, mc.font, entries, config, pos[0], pos[1]);
    }

    /** The abilities on the keys, or {@code null} if the player has none at all. */
    public static Entry @Nullable [] collect(LocalPlayer player) {
        List<KeyMapping> keys = PUKeyMappings.ALL;
        Entry[] entries = new Entry[keys.size()];
        boolean any = false;
        for (int slot = 0; slot < keys.size(); slot++) {
            for (Provider provider : PROVIDERS) {
                Entry e = provider.get(player, slot);
                if (e != null) {
                    entries[slot] = e;
                    any = true;
                    break;
                }
            }
        }
        return any ? entries : null;
    }

    /**
     * The key's label as printed on the key: letters and punctuation by their Latin key name (vanilla names them by
     * the current keyboard layout, e.g. Cyrillic letters on a Russian layout), anything else as vanilla does.
     */
    public static Component keyLabel(KeyMapping mapping) {
        String name = mapping.saveString();
        String prefix = "key.keyboard.";
        if (name.startsWith(prefix)) {
            String key = name.substring(prefix.length());
            if (key.length() == 1) {
                return Component.literal(key.toUpperCase(Locale.ROOT));
            }
            String symbol = switch (key) {
                case "comma" -> ",";
                case "period" -> ".";
                case "semicolon" -> ";";
                case "apostrophe" -> "'";
                case "slash" -> "/";
                case "backslash" -> "\\";
                case "minus" -> "-";
                case "equal" -> "=";
                case "left.bracket" -> "[";
                case "right.bracket" -> "]";
                case "grave.accent" -> "`";
                default -> null;
            };
            if (symbol != null) {
                return Component.literal(symbol);
            }
        }
        return mapping.getTranslatedKeyMessage();
    }

    private static int boxWidth(Font font, Component key) {
        return Math.max(font.width(key) + 4, 11);
    }

    /** Unscaled size {width, height} of the HUD. */
    public static int[] size(Font font, Entry[] entries, AbilityHudConfig config) {
        List<KeyMapping> keys = PUKeyMappings.ALL;
        int width = 0;
        for (int slot = 0; slot < keys.size(); slot++) {
            int w = boxWidth(font, keyLabel(keys.get(slot)));
            Entry e = slot < entries.length ? entries[slot] : null;
            if (e != null && config.showNames) {
                w += 4 + font.width(e.name());
                if (e.extra() != null) {
                    w += 4 + font.width(e.extra());
                }
            }
            width = Math.max(width, w);
        }
        return new int[] {width, keys.size() * ROW_HEIGHT - 1};
    }

    /** Screen position {x, y} of the HUD's top-left corner: the saved one, or the default (left edge, middle). */
    public static int[] position(Font font, Entry[] entries, AbilityHudConfig config, int screenWidth, int screenHeight) {
        int[] size = size(font, entries, config);
        int w = Math.round(size[0] * config.scale);
        int h = Math.round(size[1] * config.scale);
        if (!config.hasCustomPosition()) {
            return new int[] {3, screenHeight / 2 - h / 2 - 12};
        }
        int x = Math.round(config.x * screenWidth);
        int y = Math.round(config.y * screenHeight);
        return new int[] {Mth.clamp(x, 0, Math.max(0, screenWidth - w)), Mth.clamp(y, 0, Math.max(0, screenHeight - h))};
    }

    private static int alpha(int argb, float opacity) {
        int a = Math.round((argb >>> 24) * opacity);
        return a << 24 | argb & 0xFFFFFF;
    }

    /** Draws the HUD with its top-left corner at {@code x, y} (screen coordinates). */
    public static void draw(GuiGraphicsExtractor graphics, Font font, Entry[] entries, AbilityHudConfig config, int x, int y) {
        List<KeyMapping> keys = PUKeyMappings.ALL;
        float opacity = config.textOpacity;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(config.scale);
        for (int slot = 0; slot < keys.size(); slot++) {
            Entry e = slot < entries.length ? entries[slot] : null;
            int ry = slot * ROW_HEIGHT;
            Component key = keyLabel(keys.get(slot));
            int box = boxWidth(font, key);
            graphics.fill(0, ry, box, ry + 11, e != null ? 0x90000000 : 0x40000000);
            graphics.centeredText(font, key, box / 2, ry + 2, alpha(e != null ? 0xFFFFFFFF : 0x60FFFFFF, opacity));
            if (e != null && config.showNames) {
                int tx = box + 4;
                graphics.text(font, e.name(), tx, ry + 2, alpha(e.enabled() ? 0xFFFFFFFF : 0xFFA0A0A0, opacity), true);
                if (e.extra() != null) {
                    graphics.text(font, e.extra(), tx + font.width(e.name()) + 4, ry + 2, alpha(0xFFA0A0A0, opacity), true);
                }
            }
        }
        graphics.pose().popMatrix();
    }
}
