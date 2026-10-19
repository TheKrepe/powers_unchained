package net.threetag.palladium.client.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.world.entity.Avatar;
import net.threetag.palladium.client.renderer.power.ClientPowerRendererHandler;

public class PowerTransitionAnimationContainer extends AnimationContainer {

    public PowerTransitionAnimationContainer(Avatar player) {
        super(player);
    }

    @Override
    public PlayState handle(AnimationController animationController, AnimationData animationData, AnimationController.AnimationSetter animationSetter) {
        var prHandler = ClientPowerRendererHandler.get(this.getPlayer());
        var animId = prHandler != null ? prHandler.getTransitionAnimation() : null;
        int blend = animId != null ? 10 : (prHandler != null ? prHandler.getTransitionAnimationOutBlend() : 0);
        return this.setAnimation(animId, blend, animId != null, animationController, animationSetter);
    }

    @Override
    protected boolean fadeOnStop() {
        return false;
    }
}
