package net.threetag.palladium.client.gui.screen.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.icon.IconRenderer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.ModeSwitchAbility;
import net.threetag.palladium.util.Easing;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ModeSwitchOverlay implements GuiLayer {

    public static final ModeSwitchOverlay INSTANCE = new ModeSwitchOverlay();

    private static final int DISPLAY_TICKS = 60;
    private static final int FADE_TICKS = 20;
    private static final int ICON_SIZE = 48;

    private ModeSwitchAbility.Mode mode;
    private int ticksRemaining;

    public static void show(ModeSwitchAbility.Mode mode) {
        INSTANCE.mode = mode;
        INSTANCE.ticksRemaining = DISPLAY_TICKS;
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        if (INSTANCE.ticksRemaining > 0 && --INSTANCE.ticksRemaining <= 0) {
            INSTANCE.mode = null;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (this.mode == null) {
            return;
        }

        var minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        float remaining = this.ticksRemaining - deltaTracker.getGameTimeDeltaPartialTick(false);
        float alpha = Mth.clamp(remaining / FADE_TICKS, 0F, 1F);

        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2 - 20;

        int iconCenterY = centerY - ICON_SIZE / 2;
        int textY = centerY + 4;
        float scale = ICON_SIZE / 16F * Easing.INCUBIC.apply(alpha);

        final ModeSwitchAbility.Mode current = this.mode;
        current.icon().ifPresent(icon -> {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(centerX, iconCenterY);
            guiGraphics.pose().scale(scale, scale);
            IconRenderer.drawIcon(icon, minecraft, guiGraphics, DataContext.forEntity(minecraft.player), -8, -8, 1F);
            guiGraphics.pose().popMatrix();
        });
        guiGraphics.centeredText(minecraft.font, current.title(), centerX, textY, ARGB.white(alpha));
    }
}
