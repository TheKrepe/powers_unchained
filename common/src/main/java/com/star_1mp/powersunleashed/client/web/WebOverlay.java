package com.star_1mp.powersunleashed.client.web;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.entity.web.StickyWeb;
import com.star_1mp.powersunleashed.entity.web.WebCocoon;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * First person: webs stuck on your head cover the edges of the screen, a web cocoon covers all of it (like the
 * powder snow overlay).
 */
public final class WebOverlay {

    private static final Identifier TEXTURE = Constants.id("textures/misc/web_overlay.png");
    private static final Identifier TEXTURE_COCOON = Constants.id("textures/misc/web_cocoon_overlay.png");

    private WebOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        float pt = delta.getGameTimeDeltaPartialTick(false);
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();

        float amount = 0.0F;
        for (StickyWeb web : player.level().getEntitiesOfClass(StickyWeb.class, player.getBoundingBox().inflate(2.0),
                web -> web.getHitEntity() == player)) {
            if (StickyWeb.isOnHead(player, web.getHitOffset().y)) {
                float fade = Mth.clamp(1.0F - (web.tickCount + pt - 20.0F) / (web.getLifespan() - 20), 0.0F, 1.0F);
                amount += 0.45F * fade;
            }
        }
        amount = Math.min(amount, 0.9F);
        if (amount > 0.01F) {
            blit(graphics, TEXTURE, w, h, amount);
        }
        WebCocoon cocoon = WebCocoon.clientCocoon(player);
        if (cocoon != null) {
            float alpha = WebCocoon.alpha(cocoon.tickCount + pt);
            if (alpha > 0.01F) {
                blit(graphics, TEXTURE_COCOON, w, h, alpha * 0.9F);
            }
        }
    }

    private static void blit(GuiGraphicsExtractor graphics, Identifier texture, int w, int h, float alpha) {
        int color = Math.round(alpha * 255.0F) << 24 | 0xFFFFFF;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F, w, h, 256, 256, 256, 256, color);
    }
}
