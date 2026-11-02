package com.star_1mp.powersunchained.client.irradiation;

import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import com.star_1mp.powersunchained.client.power.PowersMenuScreen;
import com.star_1mp.powersunchained.power.Superpower;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

/** "Вы получили силы Человека-Паука!" + "Откройте меню способностей — J" (owner), after the Radioactive Spider's bite. */
public class PowerGainedToast implements Toast {

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_MS = 8000L;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    @Override
    public Toast.Visibility getWantedVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        this.visibility = fullyVisibleForMs >= DISPLAY_MS ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 0, 0, this.width(), this.height());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PowersMenuScreen.emblem(Superpower.SPIDERMAN), 7, 7, 18, 18);
        Component title = Component.translatable("toast.powers_unchained.power_gained." + Superpower.SPIDERMAN.id());
        java.util.List<FormattedCharSequence> lines = font.split(title, 126);
        Component hint = Component.translatable("toast.powers_unchained.power_gained.hint",
                PUKeyMappings.POWERS_MENU.getTranslatedKeyMessage());
        if (lines.size() <= 1) {
            graphics.text(font, lines.isEmpty() ? FormattedCharSequence.EMPTY : lines.get(0), 30, 7, 0xFF7CFC6A, false);
        } else {
            graphics.text(font, lines.get(0), 30, 2, 0xFF7CFC6A, false);
            graphics.text(font, lines.get(1), 30, 11, 0xFF7CFC6A, false);
        }
        graphics.text(font, font.plainSubstrByWidth(hint.getString(), 126), 30, 21, 0xFFFFFFFF, false);
    }
}
