package net.threetag.palladium.client.animation;

import net.minecraft.world.entity.Avatar;

public class TransitionAwareAnimationController extends RenderLayerAwareAnimationController {

    public TransitionAwareAnimationController(Avatar avatar, AnimationStateHandler animationHandler) {
        super(avatar, animationHandler);
    }

    @Override
    protected boolean shouldRegisterRenderLayerBones() {
        return false;
    }

    @Override
    protected boolean shouldRegisterPowerModelBones() {
        return false;
    }
}
