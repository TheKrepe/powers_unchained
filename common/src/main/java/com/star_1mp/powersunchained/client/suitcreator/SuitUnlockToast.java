package com.star_1mp.powersunchained.client.suitcreator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/** "New suit!" toast with the suit's mask, shown when a suit gets unlocked. */
public class SuitUnlockToast implements Toast {

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_MS = 5000L;

    private final ItemStack icon;
    private final Component name;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    public SuitUnlockToast(ItemStack icon, Component name) {
        this.icon = icon;
        this.name = name;
    }

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
        graphics.item(this.icon, 8, 8);
        graphics.text(font, Component.translatable("toast.powers_unchained.suit_unlocked"), 30, 7, 0xFFFFB13B, false);
        graphics.text(font, font.plainSubstrByWidth(this.name.getString(), 124), 30, 18, 0xFFFFFFFF, false);
    }
}
