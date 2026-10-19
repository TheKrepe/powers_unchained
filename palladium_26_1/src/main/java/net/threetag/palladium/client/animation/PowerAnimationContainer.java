package net.threetag.palladium.client.animation;

import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.renderer.power.PowerRenderer;
import net.threetag.palladium.client.renderer.power.PowerRendererManager;
import net.threetag.palladium.power.PowerUtil;

public class PowerAnimationContainer extends PackControllerAnimationContainer {

    public PowerAnimationContainer(PalladiumAnimationLayer layer, Avatar player) {
        super(layer, player);
    }

    @Override
    public PackAnimationController getAvailableAnimationController(Avatar player) {
        if (!(player instanceof LivingEntity living)) return null;
        var powerHandler = PowerUtil.getPowerHandler(living);
        if (powerHandler == null) return null;

        for (var powerInstance : powerHandler.getPowers()) {
            PowerRenderer renderer = PowerRendererManager.INSTANCE.get(powerInstance.getPowerId());
            if (renderer == null) continue;

            var id = renderer.getAnimationController(this.layer);
            if (id != null) {
                var controller = PackAnimationControllerManager.INSTANCE.get(id);
                if (controller != null) {
                    return controller;
                }
            }
        }

        return null;
    }
}
