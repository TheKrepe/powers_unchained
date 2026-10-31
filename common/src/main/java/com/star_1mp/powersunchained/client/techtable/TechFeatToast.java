package com.star_1mp.powersunchained.client.techtable;

import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.tech.TechFeats;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/** "Task completed!" toast (top right, owner) when a Tech Table task (feat) gets done; found by diffing the synced feats. */
public class TechFeatToast implements Toast {

    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("toast/advancement");
    private static final long DISPLAY_MS = 5000L;

    private static Set<String> known = new HashSet<>();
    private static @Nullable Player lastPlayer;
    private static long ticks;
    private static long baselineUntil;

    private final Component name;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    public TechFeatToast(Component name) {
        this.name = name;
    }

    /** Client tick. */
    public static void tick() {
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            lastPlayer = null;
            return;
        }
        Set<String> now = Services.GAMEPLAY.techProgress(player).feats();
        if (player != lastPlayer) {
            lastPlayer = player;
            baselineUntil = ticks + 60;
        }
        if (ticks < baselineUntil) {
            known = new HashSet<>(now);  // joining / respawning: already done before, no toast
            return;
        }
        for (String feat : now) {
            if (known.add(feat)) {
                mc.gui.toastManager().addToast(new TechFeatToast(TechFeats.title(feat)));
            }
        }
        known.retainAll(now);
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
        graphics.item(PUItems.WEB_SHOOTERS.get().getDefaultInstance(), 8, 8);
        graphics.text(font, Component.translatable("toast.powers_unchained.task_done"), 30, 7, 0xFF7CFC6A, false);
        graphics.text(font, font.plainSubstrByWidth(this.name.getString(), 124), 30, 18, 0xFFFFFFFF, false);
    }
}
