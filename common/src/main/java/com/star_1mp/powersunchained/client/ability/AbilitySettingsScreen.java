package com.star_1mp.powersunchained.client.ability;

import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Ability Settings: the player picks their own set of abilities for the 5 ability keys out of everything they have
 * (web-shooters, superpowers...), or None. Left: the keys; click one and pick its ability on the right. A key whose
 * ability the player doesn't have right now is greyed out here and not shown in the HUD. "Automatic" goes back to the
 * default set (everything the player has, in the default order).
 */
public class AbilitySettingsScreen extends Screen {

    private static final int W = 160;
    private static final int TOP = 40;

    private final @Nullable Screen parent;
    /** The key whose ability is being picked, -1 = none. */
    private int selected = -1;

    public AbilitySettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.powers_unchained.ability_settings"));
        this.parent = parent;
    }

    private @Nullable LocalPlayer player() {
        return this.minecraft.player;
    }

    /** The abilities to choose from: the player's own, or every ability when it has none (e.g. from the mod list). */
    private List<String> choices() {
        LocalPlayer player = this.player();
        List<String> list = player != null ? AbilitySlots.owned(player) : List.of();
        if (list.isEmpty()) {
            list = AbilitySlots.ordered();
        }
        List<String> out = new ArrayList<>();
        out.add(AbilitySlots.NONE);
        out.addAll(list);
        return out;
    }

    private Component name(String id) {
        AbilitySlots.Ability ability = AbilitySlots.get(id);
        if (ability == null) {
            return Component.translatable("screen.powers_unchained.ability_settings.none");
        }
        return ability.previewName();
    }

    private boolean has(String id) {
        AbilitySlots.Ability ability = AbilitySlots.get(id);
        LocalPlayer player = this.player();
        return ability != null && (player == null || ability.available().test(player));
    }

    private int row() {
        return Math.max(14, Math.min(24, (this.height - TOP - 40) / Math.max(6, this.choices().size())));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - W - 6;
        int right = this.width / 2 + 6;
        int row = this.row();
        int bh = Math.min(20, row - 2);
        for (int i = 0; i < PUKeyMappings.ALL.size(); i++) {
            int slot = i;
            String id = AbilitySlots.chosen(this.player(), i);
            Component label = Component.translatable("screen.powers_unchained.ability_settings.slot", i + 1,
                    AbilityHud.keyLabel(PUKeyMappings.ALL.get(i)), this.name(id).copy()
                            .withStyle(AbilitySlots.NONE.equals(id) || this.has(id) ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY));
            Button b = Button.builder(label, button -> {
                this.selected = this.selected == slot ? -1 : slot;
                this.rebuildWidgets();
            }).bounds(left, TOP + 12 + i * row, W, bh).build();
            this.addRenderableWidget(b);
        }
        if (this.selected >= 0) {
            String current = AbilitySlots.chosen(this.player(), this.selected);
            List<String> choices = this.choices();
            for (int i = 0; i < choices.size(); i++) {
                String id = choices.get(i);
                Button b = Button.builder(this.name(id), button -> {
                    AbilitySlots.choose(this.player(), this.selected, id);
                    this.selected = -1;
                    this.rebuildWidgets();
                }).bounds(right, TOP + 12 + i * row, W, bh).build();
                b.active = !id.equals(current);
                this.addRenderableWidget(b);
            }
        }
        int y = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.powers_unchained.ability_settings.auto"), button -> {
            AbilitySlots.resetChoices();
            this.selected = -1;
            this.rebuildWidgets();
        }).bounds(left, y, W, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(right, y, W, 20).build());
    }

    /** Dev/CI only: like clicking a key and then an ability. */
    public void debugChoose(int slot, String id) {
        AbilitySlots.choose(this.player(), slot, id);
        this.rebuildWidgets();
    }

    /** Dev/CI only: like clicking a key. */
    public void debugSelect(int slot) {
        this.selected = slot;
        this.rebuildWidgets();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int left = this.width / 2 - W - 6;
        int right = this.width / 2 + 6;
        int row = this.row();
        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable(AbilitySlots.custom()
                ? "screen.powers_unchained.ability_settings.custom" : "screen.powers_unchained.ability_settings.automatic"),
                this.width / 2, 24, 0xFFB0B0B0);
        graphics.text(this.font, Component.translatable("screen.powers_unchained.ability_settings.keys"), left, TOP, 0xFFFFD24A, true);
        if (this.selected >= 0) {
            int y = TOP + 12 + this.selected * row;
            int bh = Math.min(20, row - 2);
            int c = 0xFFFFD24A;
            graphics.fill(left - 2, y - 2, left + W + 2, y - 1, c);
            graphics.fill(left - 2, y + bh + 1, left + W + 2, y + bh + 2, c);
            graphics.fill(left - 2, y - 1, left - 1, y + bh + 1, c);
            graphics.fill(left + W + 1, y - 1, left + W + 2, y + bh + 1, c);
            graphics.text(this.font, Component.translatable("screen.powers_unchained.ability_settings.pick", this.selected + 1),
                    right, TOP, 0xFFFFD24A, true);
        } else {
            graphics.text(this.font, Component.translatable("screen.powers_unchained.ability_settings.hint"), right, TOP, 0xFFB0B0B0, true);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
