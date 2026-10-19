package net.threetag.palladium.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.flight.EntityFlightHandler;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class FlightCameraHandler {

    @SubscribeEvent
    static void camera(ViewportEvent.ComputeCameraAngles e) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var animHandler = EntityFlightHandler.get(player).getAnimationHandler();
        if (animHandler != null) {
            e.setRoll(e.getRoll() + animHandler.getCameraRoll((float) e.getPartialTick()));
        }
    }
}
