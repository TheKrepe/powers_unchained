package com.star_1mp.powersunchained.client.ability;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * Powers Unchained settings (the settings key in the Powers Unchained controls, or "Config" in the mod list): two
 * tabs — Interface Settings ({@link AbilityHudConfigScreen}) and Ability Settings ({@link AbilitySettingsScreen}).
 */
public class ModSettingsScreen extends Screen {

    private final @Nullable Screen parent;

    public ModSettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.powers_unchained.settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 200;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 34;
        this.addRenderableWidget(Button.builder(Component.translatable("screen.powers_unchained.settings.interface"),
                button -> this.minecraft.gui.setScreen(new AbilityHudConfigScreen(this))).bounds(x, y, w, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.powers_unchained.settings.abilities"),
                button -> this.minecraft.gui.setScreen(new AbilitySettingsScreen(this))).bounds(x, y + 24, w, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(x, y + 60, w, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFFFF);
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
