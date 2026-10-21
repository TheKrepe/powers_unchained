package com.thekrepe.powersunchained.client.ability;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Small, vanilla-styled list of the ability keys at the left edge of the screen, shown whenever the player has any
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
        if (player == null || player.isSpectator()) {
            return;
        }
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
        if (!any) {
            return;
        }
        Font font = mc.font;
        int x = 3;
        int y = graphics.guiHeight() / 2 - keys.size() * ROW_HEIGHT / 2 - 12;
        for (int slot = 0; slot < keys.size(); slot++) {
            Entry e = entries[slot];
            int ry = y + slot * ROW_HEIGHT;
            Component key = keys.get(slot).getTranslatedKeyMessage();
            int box = Math.max(font.width(key) + 4, 11);
            graphics.fill(x, ry, x + box, ry + 11, e != null ? 0x90000000 : 0x40000000);
            graphics.centeredText(font, key, x + box / 2, ry + 2, e != null ? 0xFFFFFFFF : 0x60FFFFFF);
            if (e != null) {
                int tx = x + box + 4;
                graphics.text(font, e.name(), tx, ry + 2, e.enabled() ? 0xFFFFFFFF : 0xFFA0A0A0, true);
                if (e.extra() != null) {
                    graphics.text(font, e.extra(), tx + font.width(e.name()) + 4, ry + 2, 0xFFA0A0A0, true);
                }
            }
        }
    }
}
