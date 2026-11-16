package com.star_1mp.powersunleashed.client.ability;

import com.star_1mp.powersunleashed.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Small, vanilla-styled list of the ability keys (position, size, text opacity, names and on/off set by the player
 * in {@link AbilityHudConfigScreen}) at the left edge of the screen, shown whenever the player has any
 * ability (suit power, gadget...): each key in a small dark box with the ability on it next to it (bright when the
 * ability is on, grey when off); free keys only show their dimmed box.
 * <p>
 * Abilities and their keys come from {@link AbilitySlots}.
 */
public final class AbilityHud {

    /**
     * One ability on a key.
     *
     * @param extra short value after the name (e.g. ammo), or {@code null}
     */
    public record Entry(Component name, @Nullable Component extra, boolean enabled) {
    }

    /**
     * A resource shown under the key list (e.g. the web fluid left): a 9x9 icon, value / max as small text and a thin
     * bar in {@code color}.
     */
    public record Gauge(Identifier icon, int value, int max, int color) {
    }

    /** Supplies a gauge for the player, or {@code null}. */
    public interface GaugeProvider {
        @Nullable Gauge get(LocalPlayer player);
    }

    private static final List<GaugeProvider> GAUGES = new ArrayList<>();
    /** Gauge shown in the HUD settings preview when the player has none. */
    public static @Nullable Gauge previewGauge;
    private static final float GAUGE_SCALE = 0.75F;
    private static final int GAUGE_GAP = 3;
    private static final int GAUGE_HEIGHT = 11;
    private static final int ROW_HEIGHT = 12;

    private AbilityHud() {
    }

    public static void registerGauge(GaugeProvider provider) {
        GAUGES.add(provider);
    }

    /** The first gauge of the local player (or the preview one in the HUD settings), or {@code null}. */
    public static @Nullable Gauge gauge() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            for (GaugeProvider provider : GAUGES) {
                Gauge g = provider.get(mc.player);
                if (g != null) {
                    return g;
                }
            }
        }
        return mc.gui.screen() instanceof AbilityHudConfigScreen ? previewGauge : null;
    }

    private static Component gaugeText(Gauge g) {
        return Component.literal(g.value() + " / " + g.max());
    }

    /** Unscaled width of the gauge row. */
    private static int gaugeWidth(Font font, Gauge g) {
        return 9 + 3 + Math.round(font.width(gaugeText(g)) * GAUGE_SCALE) + 3;
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
        return AbilitySlots.entries(player);
    }

    /** The same keys on a Russian (ЙЦУКЕН) keyboard: the labels follow the game language. */
    private static final java.util.Map<String, String> RU_LAYOUT = java.util.Map.ofEntries(
            java.util.Map.entry("Q", "Й"), java.util.Map.entry("W", "Ц"), java.util.Map.entry("E", "У"), java.util.Map.entry("R", "К"),
            java.util.Map.entry("T", "Е"), java.util.Map.entry("Y", "Н"), java.util.Map.entry("U", "Г"), java.util.Map.entry("I", "Ш"),
            java.util.Map.entry("O", "Щ"), java.util.Map.entry("P", "З"), java.util.Map.entry("[", "Х"), java.util.Map.entry("]", "Ъ"),
            java.util.Map.entry("A", "Ф"), java.util.Map.entry("S", "Ы"), java.util.Map.entry("D", "В"), java.util.Map.entry("F", "А"),
            java.util.Map.entry("G", "П"), java.util.Map.entry("H", "Р"), java.util.Map.entry("J", "О"), java.util.Map.entry("K", "Л"),
            java.util.Map.entry("L", "Д"), java.util.Map.entry(";", "Ж"), java.util.Map.entry("'", "Э"), java.util.Map.entry("Z", "Я"),
            java.util.Map.entry("X", "Ч"), java.util.Map.entry("C", "С"), java.util.Map.entry("V", "М"), java.util.Map.entry("B", "И"),
            java.util.Map.entry("N", "Т"), java.util.Map.entry("M", "Ь"), java.util.Map.entry(",", "Б"), java.util.Map.entry(".", "Ю"),
            java.util.Map.entry("`", "Ё"));

    private static boolean russian() {
        String lang = Minecraft.getInstance().options.languageCode;
        return lang != null && lang.startsWith("ru");
    }

    /**
     * The key's label as printed on the key (in Russian the Russian letter of that key): letters and punctuation by their Latin key name (vanilla names them by
     * the current keyboard layout, e.g. Cyrillic letters on a Russian layout), anything else as vanilla does.
     */
    public static Component keyLabel(KeyMapping mapping) {
        String name = mapping.saveString();
        String prefix = "key.keyboard.";
        if (name.startsWith(prefix)) {
            String key = name.substring(prefix.length());
            if (key.length() == 1) {
                String latin = key.toUpperCase(Locale.ROOT);
                return Component.literal(russian() ? RU_LAYOUT.getOrDefault(latin, latin) : latin);
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
                return Component.literal(russian() ? RU_LAYOUT.getOrDefault(symbol, symbol) : symbol);
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
        int rows = 0;
        for (int slot = 0; slot < keys.size(); slot++) {
            Entry e = slot < entries.length ? entries[slot] : null;
            if (e == null) {
                continue;  // keys without an ability are not shown
            }
            rows++;
            int w = boxWidth(font, keyLabel(keys.get(slot)));
            if (config.showNames) {
                w += 4 + font.width(e.name());
                if (e.extra() != null) {
                    w += 4 + font.width(e.extra());
                }
            }
            width = Math.max(width, w);
        }
        int height = Math.max(rows * ROW_HEIGHT - 1, 0);
        Gauge g = gauge();
        if (g != null) {
            width = Math.max(width, gaugeWidth(font, g));
            height += (rows > 0 ? GAUGE_GAP : 0) + GAUGE_HEIGHT;
        }
        return new int[] {width, height};
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
        int row = 0;
        for (int slot = 0; slot < keys.size(); slot++) {
            Entry e = slot < entries.length ? entries[slot] : null;
            if (e == null) {
                continue;  // keys without an ability are not shown
            }
            int ry = row++ * ROW_HEIGHT;
            Component key = keyLabel(keys.get(slot));
            int box = boxWidth(font, key);
            graphics.fill(0, ry, box, ry + 11, 0x90000000);
            graphics.centeredText(font, key, box / 2, ry + 2, alpha(0xFFFFFFFF, opacity));
            if (config.showNames) {
                int tx = box + 4;
                graphics.text(font, e.name(), tx, ry + 2, alpha(e.enabled() ? 0xFFFFFFFF : 0xFFA0A0A0, opacity), true);
                if (e.extra() != null) {
                    graphics.text(font, e.extra(), tx + font.width(e.name()) + 4, ry + 2, alpha(0xFFA0A0A0, opacity), true);
                }
            }
        }
        Gauge g = gauge();
        if (g != null) {
            drawGauge(graphics, font, g, row > 0 ? row * ROW_HEIGHT - 1 + GAUGE_GAP : 0, opacity);
        }
        graphics.pose().popMatrix();
    }

    /**
     * The gauge row: a dark box with the icon, the small "value / max" text and a thin bar under it that empties
     * (red when nearly empty).
     */
    private static void drawGauge(GuiGraphicsExtractor graphics, Font font, Gauge g, int y, float opacity) {
        int w = gaugeWidth(font, g);
        graphics.fill(0, y, w, y + GAUGE_HEIGHT, 0x90000000);
        graphics.blit(RenderPipelines.GUI_TEXTURED, g.icon(), 1, y + 1, 0.0F, 0.0F, 9, 9, 9, 9, 9, 9, alpha(0xFFFFFFFF, opacity));
        float fill = g.max() > 0 ? Mth.clamp(g.value() / (float) g.max(), 0.0F, 1.0F) : 0.0F;
        boolean low = fill < 0.15F;
        int tx = 12;
        graphics.pose().pushMatrix();
        graphics.pose().translate(tx, y + 1.5F);
        graphics.pose().scale(GAUGE_SCALE);
        graphics.text(font, gaugeText(g), 0, 0, alpha(low ? 0xFFFF6A5A : 0xFFE8F8FF, opacity), true);
        graphics.pose().popMatrix();
        int bx0 = tx;
        int bx1 = w - 3;
        int by = y + GAUGE_HEIGHT - 2;
        graphics.fill(bx0, by, bx1, by + 1, alpha(0x60FFFFFF, opacity));
        int bx = bx0 + Math.round((bx1 - bx0) * fill);
        if (bx > bx0) {
            graphics.fill(bx0, by, bx, by + 1, alpha(low ? 0xFFFF5040 : g.color(), opacity));
        }
    }
}
