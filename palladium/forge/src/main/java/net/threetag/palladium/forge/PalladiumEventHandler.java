package net.threetag.palladium.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.PalladiumClient;
import net.threetag.palladium.PalladiumConfig;
import net.threetag.palladium.client.screen.components.IconButton;
import net.threetag.palladium.client.screen.hub.AddonHubScreen;
import net.threetag.palladium.entity.PalladiumAttributes;

@Mod.EventBusSubscriber(modid = Palladium.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PalladiumEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed e) {
        if (e.getEntity().getAttributes().hasAttribute(PalladiumAttributes.DESTROY_SPEED.get())) {
            e.setNewSpeed((float) (e.getNewSpeed() * e.getEntity().getAttributeValue(PalladiumAttributes.DESTROY_SPEED.get())));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void addButtons(ScreenEvent.Init.Post e) {
        if (e.getScreen() instanceof TitleScreen && !PalladiumConfig.Client.DISABLE_PALLADIUM_HUB_BUTTON.get()) {
            int l = e.getScreen().height / 4 + 48;
            int y = l + 48;
            int x = PalladiumClient.findFreeSpaceToTheLeft(e.getScreen(), e.getScreen().width / 2 - 100 - PalladiumClient.BUTTON_GAP - 20, y, 20, 20);

            e.addListener(IconButton.builder(PalladiumClient.ICON, button ->
                            Minecraft.getInstance().setScreen(new AddonHubScreen(e.getScreen())))
                    .pos(x, y)
                    .enableDefaultBackground()
                    .tooltip(Tooltip.create(Component.translatable(AddonHubScreen.TITLE_KEY))).build());
        }
    }

}
