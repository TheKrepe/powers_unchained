package net.threetag.palladium.client.gui.screen.hub;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ModsButton;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.widget.IconButton;
import net.threetag.palladium.icon.SpriteIcon;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class AddonHubButtonInjection {

    @SuppressWarnings("UnstableApiUsage")
    @SubscribeEvent
    static void screenInit(ScreenEvent.Init.Post e) {
        if (e.getScreen() instanceof TitleScreen titleScreen) {
            for (Renderable renderable : titleScreen.renderables) {
                if (renderable instanceof ModsButton mods) {
                    Button button = IconButton.builder(new SpriteIcon(Palladium.id("icon/palladium")),
                                    b -> titleScreen.getMinecraft().setScreen(new AddonHubScreen(titleScreen)))
                            .pos(mods.getX() + mods.getWidth() + 4, mods.getY())
                            .tooltip(Tooltip.create(Component.translatable(AddonHubScreen.TITLE_KEY)))
                            .build();
                    titleScreen.addRenderableWidget(button);
                    return;
                }
            }
        }
    }
}
