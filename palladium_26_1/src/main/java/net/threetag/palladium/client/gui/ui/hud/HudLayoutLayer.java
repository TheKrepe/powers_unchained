package net.threetag.palladium.client.gui.ui.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.threetag.palladium.client.gui.ui.layout.UiLayoutManager;
import net.threetag.palladium.client.renderer.power.PowerRendererManager;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.PowerUtil;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HudLayoutLayer implements GuiLayer {

    public static final HudLayoutLayer INSTANCE = new HudLayoutLayer();

    private final Map<Identifier, HudUiScreen> hosts = new HashMap<>();

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;

        if (player == null || minecraft.options.hideGui) {
            return;
        }

        var handler = PowerUtil.getPowerHandler(player);

        if (handler == null) {
            this.hosts.clear();
            return;
        }

        var context = DataContext.forEntity(player);
        Set<Identifier> active = new LinkedHashSet<>();

        for (PowerInstance power : handler.getPowers()) {
            var renderer = PowerRendererManager.INSTANCE.get(power.getPowerId());

            if (renderer == null) {
                continue;
            }

            renderer.hudLayouts().forEach((layoutId, condition) -> {
                if (condition.test(context)) {
                    active.add(layoutId);
                }
            });
        }

        this.hosts.keySet().retainAll(active);

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        for (Identifier layoutId : active) {
            var layout = UiLayoutManager.INSTANCE.get(layoutId);

            if (layout == null) {
                this.hosts.remove(layoutId);
                continue;
            }

            var host = this.hosts.get(layoutId);

            if (host == null || !host.matches(layout, width, height)) {
                host = new HudUiScreen(layout, width, height);
                this.hosts.put(layoutId, host);
            }

            host.render(guiGraphics, partialTick);
        }
    }
}
