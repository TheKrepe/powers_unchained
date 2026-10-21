package com.thekrepe.powersunchained.client.ability;

import com.thekrepe.powersunchained.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
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
 * Always-on list of the ability keys at the left edge of the screen, shown whenever the player has any ability
 * (suit power, gadget...). Every ability key gets a row: its key, the ability on it and its state; free keys are
 * listed dimmed. Extra control hints (e.g. "Right Click - Shoot Web") go below the rows.
 * <p>
 * Abilities come from {@link Provider}s, so new powers only register a provider.
 */
public final class AbilityHud {

    /** One ability on a key. */
    public record Entry(Component name, @Nullable Component status, boolean enabled) {
    }

    /** Supplies the ability of a key slot (0-based) for the player, or {@code null} if it has none there. */
    public interface Provider {
        @Nullable Entry get(LocalPlayer player, int slot);

        /** Extra control hints (key, description) shown under the ability rows. */
        default void hints(LocalPlayer player, List<Component[]> out) {
        }
    }

    private static final List<Provider> PROVIDERS = new ArrayList<>();

    private static final int BOX_COLOR = 0x3A4E6E;
    private static final int ROW_HEIGHT = 11;

    private AbilityHud() {
    }

    public static void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    private static int argb(int rgb, float alpha) {
        return Math.round(Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24 | rgb & 0xFFFFFF;
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
        List<Component[]> hints = new ArrayList<>();
        for (Provider provider : PROVIDERS) {
            provider.hints(player, hints);
        }

        Font font = mc.font;
        // build the rows: [key] name   status
        Component[] keyText = new Component[keys.size()];
        Component[] rowText = new Component[keys.size()];
        int keyWidth = 0;
        int rowWidth = 0;
        for (int slot = 0; slot < keys.size(); slot++) {
            Entry e = entries[slot];
            keyText[slot] = keys.get(slot).getTranslatedKeyMessage().copy()
                    .withStyle(e != null ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY);
            if (e == null) {
                rowText[slot] = Component.literal("—").withStyle(ChatFormatting.DARK_GRAY);
            } else {
                var row = Component.empty().append(e.name().copy().withStyle(e.enabled() ? ChatFormatting.WHITE : ChatFormatting.GRAY));
                if (e.status() != null) {
                    row.append(Component.literal("  ")).append(e.status());
                }
                rowText[slot] = row;
            }
            keyWidth = Math.max(keyWidth, font.width(keyText[slot]));
            rowWidth = Math.max(rowWidth, font.width(rowText[slot]));
        }
        Component[] hintText = new Component[hints.size()];
        for (int i = 0; i < hints.size(); i++) {
            hintText[i] = Component.empty()
                    .append(hints.get(i)[0].copy().withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(hints.get(i)[1].copy().withStyle(ChatFormatting.GRAY));
        }

        int keyBox = Math.max(keyWidth + 4, 11);
        int x = 4;
        int height = keys.size() * ROW_HEIGHT + 4;
        int y = graphics.guiHeight() / 2 - height / 2 - 10;
        int width = keyBox + 4 + rowWidth + 4;

        graphics.fill(x - 2, y - 2, x + width, y + height - 2, argb(BOX_COLOR, 0.45F));
        for (int slot = 0; slot < keys.size(); slot++) {
            int ry = y + slot * ROW_HEIGHT;
            boolean has = entries[slot] != null;
            graphics.fill(x, ry, x + keyBox, ry + ROW_HEIGHT - 1, argb(0x000000, has ? 0.55F : 0.3F));
            graphics.centeredText(font, keyText[slot], x + keyBox / 2, ry + 1, 0xFFFFFFFF);
            graphics.text(font, rowText[slot], x + keyBox + 4, ry + 1, 0xFFFFFFFF, true);
        }
        int hy = y + height + 1;
        for (Component hint : hintText) {
            int w = font.width(hint);
            graphics.fill(x - 2, hy - 2, x + w + 2, hy + 9, argb(BOX_COLOR, 0.35F));
            graphics.text(font, hint, x, hy, 0xFFFFFFFF, true);
            hy += 12;
        }
    }
}
